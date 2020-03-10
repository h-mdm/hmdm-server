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

package com.hmdm.persistence.mapper;

import com.hmdm.persistence.domain.UploadedFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

/**
 * <p>An ORM mapper for {@link UploadedFile} domain objects.</p>
 *
 * @author isv
 */
public interface UploadedFileMapper {

    @Insert("INSERT INTO uploadedFiles (customerId, filePath, uploadTime) VALUES (#{customerId}, #{filePath}, EXTRACT(EPOCH FROM NOW()) * 1000)")
    @SelectKey(statement = "SELECT currval('uploadedFiles_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class)
    int insertFile(UploadedFile file);

    @Select("SELECT * FROM uploadedFiles WHERE id = #{id}")
    UploadedFile getFileById(@Param("id") Integer id);

    @Delete("DELETE FROM uploadedFiles WHERE id = #{id}")
    void deleteFile(@Param("id") int fileId);
}
