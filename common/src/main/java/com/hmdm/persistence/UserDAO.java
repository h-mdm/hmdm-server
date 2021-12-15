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
import javax.inject.Named;

import com.google.inject.Singleton;
import org.mybatis.guice.transactional.Transactional;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.mapper.UserMapper;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class UserDAO extends AbstractDAO<User> {
    private final UserMapper mapper;
    private final int orgAdminRoleId;

    @Inject
    public UserDAO(UserMapper mapper, @Named("role.orgadmin.id") int orgAdminRoleId) {
        this.mapper = mapper;
        this.orgAdminRoleId = orgAdminRoleId;
    }

    public User findByLoginOrEmail( String login ) {
        return getSingleRecord(() -> {
            User user = mapper.findByLogin(login);
            if (user == null) {
                user = mapper.findByEmail(login);
            }
            return user;
        }, SecurityException::onUserAccessViolation);
    }

    public User getUserDetails( int id ) {
        return getSingleRecord(() -> mapper.findById( id ), SecurityException::onUserAccessViolation);
    }


    @Transactional
    public void updatePassword(User user ) {
        updateRecord(user, this.mapper::updatePassword, SecurityException::onUserAccessViolation);
    }

    @Transactional
    public void updatePasswordBySuperAdmin(User user ) {
        if (SecurityContext.get().isSuperAdmin()) {
            this.mapper.setNewPassword(user);
        } else {
            throw new IllegalArgumentException("Super-admin is allowed only");
        }
    }

    public void insert(User user) {
        this.mapper.insert(user);
        if (!user.isAllDevicesAvailable()) {
            List<LookupItem> groups = user.getGroups();
            if (groups != null && !groups.isEmpty()) {
                this.mapper.insertUserDeviceGroupsAccess(user.getId(), groups.stream().map(LookupItem::getId).collect(Collectors.toList()));
            }
        }
    }

    public List<User> findAllUsers() {
        return getList(this.mapper::findAll);
    }

    public List<User> findAllUsers(String value) {
        return getList(customerId -> this.mapper.findAllByFilter(customerId, value));
    }

    public void updateUserMainDetails(User user) {
        updateRecord(user, u -> {
            this.mapper.updateUserMainDetails(u);
            this.mapper.removeDeviceGroupsAccessByUserId(u.getCustomerId(), u.getId());
            if (!u.isAllDevicesAvailable()) {
                List<LookupItem> groups = u.getGroups();
                if (groups != null && !groups.isEmpty()) {
                    this.mapper.insertUserDeviceGroupsAccess(u.getId(), groups.stream().map(LookupItem::getId).collect(Collectors.toList()));
                }
            }
        }, SecurityException::onUserAccessViolation);
    }

    public void deleteUser(int id) {
        SecurityContext.get().getCurrentUser().ifPresent(current -> {
            if (current.getId() == id) {
                throw new IllegalArgumentException("Can't remove self");
            } else {
                updateById(id, mapper::findById, this.mapper::deleteUser, SecurityException::onUserAccessViolation);
            }
        });
    }

    public User findOrgAdmin(Integer id) {
        return mapper.findAll(id).stream().filter(u -> u.getUserRole().getId() == this.orgAdminRoleId).findFirst().orElse(null);
    }

    public boolean isOrgAdmin(User user) {
        return user.getUserRole().getId() == this.orgAdminRoleId;
    }

    public List<UserRole> findAllUserRoles() {
        return SecurityContext.get().getCurrentUser()
                .map(u -> mapper.findAllUserRoles(u.getUserRole().isSuperAdmin()))
                .orElse(new ArrayList<>());
    }

    public List<User> findAllCustomerUsers(int customerId) {
        if (SecurityContext.get().isSuperAdmin()) {
            return this.mapper.findAll(customerId);
        } else {
            throw new IllegalArgumentException("Super-admin is allowed only");
        }
    }

    /**
     * <p>Gets the list of identifiers for hints already show to current user.</p>
     *
     * @return a list of shown hint identifiers.
     */
    public List<String> getShownHints() {
        return SecurityContext.get().getCurrentUser()
                .map(user -> this.mapper.getShownHints(user.getId()))
                .orElseThrow(SecurityException::onAnonymousAccess);
    }

    /**
     * <p>Marks specified hint as shown to current user.</p>
     *
     * @param hintKey an identifier of the hint.
     */
    public void onHintShown(String hintKey) {
        SecurityContext.get().getCurrentUser()
                .map(user -> this.mapper.insertShownHint(user.getId(), hintKey))
                .orElseThrow(SecurityException::onAnonymousAccess);
    }

    /**
     * <p>Clears the list of identifiers for hints already show to current user.</p>
     */
    public void enableHints() {
        SecurityContext.get().getCurrentUser()
                .map(user -> this.mapper.clearHintsHistory(user.getId()))
                .orElseThrow(SecurityException::onAnonymousAccess);
    }

    /**
     * <p>Clears the list of identifiers for hints already show to current user.</p>
     */
    @Transactional
    public void disableHints() {
        SecurityContext.get().getCurrentUser()
                .map(user -> {
                    this.mapper.clearHintsHistory(user.getId());
                    this.mapper.insertHintsHistoryAll(user.getId());
                    return Optional.empty();
                })
                .orElseThrow(SecurityException::onAnonymousAccess);
    }
}
