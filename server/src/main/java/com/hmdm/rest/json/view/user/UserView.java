package com.hmdm.rest.json.view.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.rest.json.LookupItem;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * <p>A wrapper around the {@link User} object providing the view returned by the login response</p>
 *
 * @author seva
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserView {
    private final User user;

    public UserView(User user) {
        this.user = user;
    }

    public Integer getId() {
        return user.getId();
    }
    public String getLogin() {
        return user.getLogin();
    }
    public String getEmail() {
        return user.getEmail();
    }
    public String getName() {
        return user.getName();
    }
    public int getCustomerId() {
        return user.getCustomerId();
    }
    public boolean isMasterCustomer() {
        return user.isMasterCustomer();
    }
    public boolean isEditable() {
        return user.isEditable();
    }
    public boolean isSingleCustomer() {
        return user.isSingleCustomer();
    }
    public UserRoleView getUserRole() {
        return new UserRoleView(user.getUserRole());
    }
    public boolean isSuperAdmin() {
        return user.getUserRole().isSuperAdmin();
    }
    public boolean isAllDevicesAvailable() {
        return user.isAllDevicesAvailable();
    }
    public boolean isAllConfigAvailable() {
        return user.isAllConfigAvailable();
    }
    public boolean isPasswordReset() {
        return user.isPasswordReset();
    }
    public String getAuthToken() {
        return user.getAuthToken();
    }
    public String getPasswordResetToken() {
        return user.getPasswordResetToken();
    }
    public List<LookupItem> getGroups() {
        return user.getGroups();
    }
    public List<LookupItem> getConfigurations() {
        return user.getConfigurations();
    }
    public Boolean getTwoFactor() {
        return user.isTwoFactor() ? true : null;
    }
    public Boolean getTwoFactorAccepted() {
        return user.isTwoFactorAccepted() ? true : null;
    }
    public Integer getIdleLogout() {
        return user.getIdleLogout();
    }
}
