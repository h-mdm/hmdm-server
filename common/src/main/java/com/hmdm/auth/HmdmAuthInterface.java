package com.hmdm.auth;

import com.hmdm.persistence.domain.User;

public interface HmdmAuthInterface {
    User findUser(String login);
    boolean authenticate(User user, String password);
}
