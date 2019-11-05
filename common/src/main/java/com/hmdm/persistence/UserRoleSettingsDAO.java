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
import com.hmdm.persistence.domain.UserRoleSettings;
import com.hmdm.persistence.mapper.UserRoleSettingsMapper;
import com.hmdm.security.SecurityContext;
import org.mybatis.guice.transactional.Transactional;

import java.util.List;

/**
 * <p>A DAO used for managing the user role settings data.</p>
 *
 * @author isv
 */
@Singleton
public class UserRoleSettingsDAO extends AbstractDAO<UserRoleSettings> {

    private final UserRoleSettingsMapper mapper;

    /**
     * <p>Constructs new <code>UserRoleSettingsDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public UserRoleSettingsDAO(UserRoleSettingsMapper mapper) {
        this.mapper = mapper;
    }

    public UserRoleSettings getUserRoleSettings(int roleId) {
        return getSingleRecordWithCurrentUser(
                u -> this.mapper.getUserRoleSettings(u.getCustomerId(), roleId)
        );
    }

    @Transactional
    public void saveCommonSettings(List<UserRoleSettings> settings) {
        SecurityContext.get().getCurrentUser().ifPresent(u -> settings.forEach(s -> {
            s.setCustomerId(u.getCustomerId());
            this.mapper.saveUserRoleCommonSettings(s);
        }));
    }
}
