package com.hmdm.rest.json.view.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.domain.UserRolePermission;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>A wrapper around the {@link UserRole} object providing the view returned by the login response</p>
 *
 * @author seva
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRoleView {
    private final UserRole userRole;

    public UserRoleView(UserRole userRole) {
        this.userRole = userRole;
    }

    public int getId() {
        return userRole.getId();
    }
    public String getName() {
        return userRole.getName();
    }
    public boolean isSuperAdmin() {
        return userRole.isSuperAdmin();
    }
    public List<PermissionView> getPermissions() {
        List<PermissionView> result = new LinkedList<>();
        for (UserRolePermission permission : userRole.getPermissions()) {
            result.add(new PermissionView(permission));
        }
        return result;
    }
}
