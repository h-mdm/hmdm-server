/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.domain.UserRolePermission;
import com.hmdm.persistence.mapper.UserRoleMapper;
import com.hmdm.security.SecurityContext;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class UserRoleDAO {
    private final UnsecureDAO unsecureDAO;
    private final UserDAO userDAO;
    private final UserRoleMapper mapper;
    private final int orgAdminRoleId;

    @Inject
    public UserRoleDAO(UnsecureDAO unsecureDAO,
                       UserDAO userDAO,
                       UserRoleMapper mapper,
                       @Named("role.orgadmin.id") int orgAdminRoleId) {
        this.unsecureDAO = unsecureDAO;
        this.userDAO = userDAO;
        this.mapper = mapper;
        this.orgAdminRoleId = orgAdminRoleId;
    }

    public boolean hasAccess() {
        return SecurityContext.get().getCurrentUser().map(u -> {
            if (unsecureDAO.isSingleCustomer()) {
                if (!u.getUserRole().isSuperAdmin() && !userDAO.isOrgAdmin(u)) {
                    return false;
                }
            } else if (!u.getUserRole().isSuperAdmin()) {
                return false;
            }
            return true;
        }).orElse(false);
    }

    void checkAccess() {
        if (!hasAccess()) {
            throw new IllegalArgumentException("Operation not allowed");
        }
    }

    public List<UserRolePermission> getPermissionsList() {
        checkAccess();
        return mapper.getPermissionsList();
    }

    public List<UserRole> findAll() {
        checkAccess();
        List<UserRole> roles = mapper.findAll();
        roles.removeIf(role -> (role.getId() == orgAdminRoleId));   // Admin cannot edit admin permissions!
        return roles;
    }

    public UserRole findByName(String name) {
        checkAccess();
        return mapper.findByName(name);
    }

    public UserRole findById(Integer id) {
        checkAccess();
        return mapper.findById(id);
    }

    public void insert(UserRole userRole) {
        checkAccess();
        mapper.insert(userRole);
        if (userRole.getPermissions() != null && userRole.getPermissions().size() > 0) {
            mapper.insertPermissions(userRole.getId(),
                    userRole.getPermissions().stream().map(UserRolePermission::getId).collect(Collectors.toList()));
        }
    }

    public void update(UserRole userRole) {
        checkAccess();
        mapper.update(userRole);
        mapper.deletePermissions(userRole.getId());
        if (userRole.getPermissions() != null && userRole.getPermissions().size() > 0) {
            mapper.insertPermissions(userRole.getId(),
                    userRole.getPermissions().stream().map(UserRolePermission::getId).collect(Collectors.toList()));
        }
    }

    public void delete(int id) {
        checkAccess();
        if (id != orgAdminRoleId) {
            mapper.delete(id);
        } else {
            throw new IllegalArgumentException("Cannot delete the admin role");
        }
    }
}
