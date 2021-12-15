package com.hmdm.guice.module;

import com.google.inject.Inject;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.util.PasswordUtil;

import java.util.List;

public class StartupTaskModule {

    private UnsecureDAO userDAO;
    private BackgroundTaskRunnerService taskRunner;

    /**
     * <p>Constructs new <code>StartupTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public StartupTaskModule(UnsecureDAO userDAO, BackgroundTaskRunnerService taskRunner) {
        this.userDAO = userDAO;
        this.taskRunner = taskRunner;
    }

    public void init() {
        taskRunner.submitTask(new UpdatePasswordTask());
    }

    public class UpdatePasswordTask implements Runnable {

        @Override
        public void run() {
            List<User> users = userDAO.findAllWithOldPassword();
            for (User user : users) {
                user.setNewPassword(PasswordUtil.getHashFromMd5(user.getPassword().toUpperCase()));
                userDAO.setUserNewPasswordUnsecure(user);
            }
        }
    }
}
