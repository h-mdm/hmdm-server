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
import com.hmdm.persistence.domain.UploadedFile;
import com.hmdm.persistence.mapper.IconMapper;
import com.hmdm.persistence.mapper.UploadedFileMapper;
import com.hmdm.security.SecurityException;

/**
 * <p>A DAO used for managing the icon data.</p>
 *
 * @author isv
 */
@Singleton
public class UploadedFileDAO extends AbstractDAO<UploadedFile> {

    /**
     * <p>An interface to file data persistence layer.</p>
     */
    private final UploadedFileMapper fileMapper;

    /**
     * <p>Constructs new <code>UploadedFileDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public UploadedFileDAO(UploadedFileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    /**
     * <p>Inserts new record for the specified uploaded file.</p>
     *
     * @param file an uploaded filed to be inserted into DB.
     * @return a created file.
     */
    public UploadedFile insertFile(UploadedFile file) {
        insertRecord(file, this.fileMapper::insertFile);
        return getSingleRecord(() -> this.fileMapper.getFileById(file.getId()), SecurityException::onUploadedFileAccessViolation);
    }


}
