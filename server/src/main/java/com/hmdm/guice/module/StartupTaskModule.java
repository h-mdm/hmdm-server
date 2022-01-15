package com.hmdm.guice.module;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.util.PasswordUtil;

import java.util.List;

public class StartupTaskModule {

    private UnsecureDAO unsecureDAO;
    private BackgroundTaskRunnerService taskRunner;
    private int deviceFastSearchChars;

    /**
     * <p>Constructs new <code>StartupTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public StartupTaskModule(UnsecureDAO unsecureDAO, BackgroundTaskRunnerService taskRunner,
                             @Named("device.fast.search.chars") int deviceFastSearchChars) {
        this.unsecureDAO = unsecureDAO;
        this.taskRunner = taskRunner;
        this.deviceFastSearchChars = deviceFastSearchChars;
    }

    public void init() {
        taskRunner.submitTask(new UpdatePasswordTask());
        taskRunner.submitTask(new UpdateDeviceFastSearchTask());
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
}
