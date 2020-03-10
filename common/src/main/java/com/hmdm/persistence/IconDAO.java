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
import com.hmdm.persistence.domain.Icon;
import com.hmdm.persistence.mapper.IconMapper;
import com.hmdm.security.SecurityException;

import java.util.List;

/**
 * <p>A DAO used for managing the icon data.</p>
 *
 * @author isv
 */
@Singleton
public class IconDAO extends AbstractDAO<Icon> {

    /**
     * <p>An interface to icon data persistence layer.</p>
     */
    private final IconMapper iconMapper;

    /**
     * <p>Constructs new <code>IconDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public IconDAO(IconMapper iconMapper) {
        this.iconMapper = iconMapper;
    }

    /**
     * <p>Inserts new record for the specified icon.</p>
     *
     * @param icon an icon to be inserted into DB.
     * @return a created icon.
     */
    public Icon insertIcon(Icon icon) {
        insertRecord(icon, this.iconMapper::insertIcon);
        return getSingleRecord(() -> this.iconMapper.getIconById(icon.getId()), SecurityException::onIconAccessViolation);
    }

    /**
     * <p>Gets all the icons for the current customer</p>
     *
     * @return a list of icons.
     */
    public List<Icon> getAllIcons() {
        return getList(this.iconMapper::getAllIcons);
    }

    public boolean isFileUsed(String fileName) {
        return this.iconMapper.countFileUsedAsIcon(fileName) > 0;
    }

    public List<String> getUsingIcons(Integer customerId, String fileName) {
        return this.iconMapper.getUsingIcons(customerId, fileName);
    }
}
