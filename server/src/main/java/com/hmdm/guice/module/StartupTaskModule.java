package com.hmdm.guice.module;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.event.EventService;
import com.hmdm.persistence.CommonDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class StartupTaskModule {

    private CommonDAO commonDAO;
    private UnsecureDAO unsecureDAO;
    private BackgroundTaskRunnerService taskRunner;
    private int deviceFastSearchChars;
    private String sqlInitScriptPath;

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    /**
     * <p>Constructs new <code>StartupTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public StartupTaskModule(CommonDAO commonDAO, UnsecureDAO unsecureDAO, BackgroundTaskRunnerService taskRunner,
                             @Named("device.fast.search.chars") int deviceFastSearchChars,
                             @Named("sql.init.script.path") String sqlInitScriptPath) {
        this.commonDAO = commonDAO;
        this.unsecureDAO = unsecureDAO;
        this.taskRunner = taskRunner;
        this.deviceFastSearchChars = deviceFastSearchChars;
        this.sqlInitScriptPath = sqlInitScriptPath;
    }

    public void init() {
        taskRunner.submitTask(new UpdatePasswordTask());
        taskRunner.submitTask(new UpdateDeviceFastSearchTask());
        if (!sqlInitScriptPath.equals("")) {
            taskRunner.submitTask(new ExecuteInitSqlTask());
        }
    }

    public class UpdatePasswordTask implements Runnable {

        @Override
        public void run() {
            List<User> users = unsecureDAO.findAllWithOldPassword();
            for (User user : users) {
                user.setNewPassword(PasswordUtil.getHashFromMd5(user.getPassword().toUpperCase()));
                unsecureDAO.setUserNewPasswordUnsecure(user);
            }
        }
    }

    public class UpdateDeviceFastSearchTask implements Runnable {
        @Override
        public void run() {
            unsecureDAO.updateDeviceFastSearch(deviceFastSearchChars);
        }
    }

    public class ExecuteInitSqlTask implements Runnable {

        @Override
        public void run() {
            if (!commonDAO.isDatabaseInitialized()) {
                try
                {
                    byte[] bytes = Files.readAllBytes(Paths.get(sqlInitScriptPath));
                    String sqlContent = new String(bytes, StandardCharsets.UTF_8);
                    commonDAO.executeRawQuery(sqlContent);
                }
                catch (Exception e)
                {
                    logger.error("Failed to execute startup SQL script " + sqlInitScriptPath + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
