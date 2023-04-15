package com.hmdm.rest.json.view.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.UserRolePermission;

/**
 * <p>A wrapper around the {@link UserRolePermission} object providing the view returned by the login response</p>
 *
 * @author seva
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionView {
    private final UserRolePermission permission;

    public PermissionView(UserRolePermission permission) {
        this.permission = permission;
    }

    public String getName() {
        return permission.getName();
    }
}
