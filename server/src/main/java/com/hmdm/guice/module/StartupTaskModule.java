package com.hmdm.guice.module;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.event.EventService;
import com.hmdm.persistence.CommonDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.service.RsaKeyService;
import com.hmdm.task.CustomerStatusTask;
import com.hmdm.task.FileCheckTask;
import com.hmdm.task.FileMigrateTask;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StartupTaskModule {

    private CommonDAO commonDAO;
    private UnsecureDAO unsecureDAO;
    private BackgroundTaskRunnerService taskRunner;
    private int deviceFastSearchChars;
    private String sqlInitScriptPath;
    private CustomerStatusTask customerStatusTask;
    private FileCheckTask fileCheckTask;
    private FileMigrateTask fileMigrateTask;
    private boolean customerAutoStatus;
    private boolean transmitPassword;
    private RsaKeyService rsaKeyService;

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    /**
     * <p>Constructs new <code>StartupTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public StartupTaskModule(CommonDAO commonDAO, UnsecureDAO unsecureDAO, BackgroundTaskRunnerService taskRunner,
                             CustomerStatusTask customerStatusTask,
                             FileCheckTask fileCheckTask,
                             FileMigrateTask fileMigrateTask,
                             RsaKeyService rsaKeyService,
                             @Named("device.fast.search.chars") int deviceFastSearchChars,
                             @Named("sql.init.script.path") String sqlInitScriptPath,
                             @Named("customer.auto.status") boolean customerAutoStatus,
                             @Named("transmit.password") boolean transmitPassword) {
        this.commonDAO = commonDAO;
        this.unsecureDAO = unsecureDAO;
        this.taskRunner = taskRunner;
        this.deviceFastSearchChars = deviceFastSearchChars;
        this.sqlInitScriptPath = sqlInitScriptPath;
        this.customerStatusTask = customerStatusTask;
        this.fileCheckTask = fileCheckTask;
        this.fileMigrateTask = fileMigrateTask;
        this.customerAutoStatus = customerAutoStatus;
        this.transmitPassword = transmitPassword;
        this.rsaKeyService = rsaKeyService;
    }

    public void init() {
        taskRunner.submitTask(new UpdatePasswordTask());
        taskRunner.submitTask(new UpdateDeviceFastSearchTask());
        taskRunner.submitTask(new ResetUserLoginFailTimeTask());
        taskRunner.submitTask(fileMigrateTask);
        if (!sqlInitScriptPath.equals("")) {
            taskRunner.submitTask(new ExecuteInitSqlTask());
        }
        if (customerAutoStatus) {
            taskRunner.submitRepeatableTask(customerStatusTask, 0, 1, TimeUnit.HOURS);
        }
        // Shift a task to 5 min so they won't execute at the same time
        taskRunner.submitRepeatableTask(fileCheckTask, 5, 60, TimeUnit.MINUTES);
        if (transmitPassword) {
            taskRunner.submitTask(new GenerateRsaKeysTask());
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

    // Reset login fail times to avoid permanent auth failure if a server time occasionally changes
    public class ResetUserLoginFailTimeTask implements Runnable {
        @Override
        public void run() {
            unsecureDAO.resetUserLoginFailTime();
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

    public class GenerateRsaKeysTask implements Runnable {
        @Override
        public void run() {
            // This method generates keys if they do not yet exist
            rsaKeyService.getPrivateKey();
        }
    }
}
