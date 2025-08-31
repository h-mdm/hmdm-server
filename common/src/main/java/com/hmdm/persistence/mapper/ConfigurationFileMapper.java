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

import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.ConfigurationFile;
import com.hmdm.persistence.domain.UploadedFile;
import com.hmdm.rest.json.FileConfigurationLink;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>An ORM mapper for the {@link com.hmdm.persistence.domain.ConfigurationFile} objects.</p>
 */
public interface ConfigurationFileMapper {

    // Used only in the migration task from old to new method of storing configuration files
    @Select("SELECT * FROM configurationFiles WHERE devicePath IS NOT NULL")
    @Deprecated
    List<ConfigurationFile> getOldStyleConfigurationFiles();

    @Select("SELECT cf.id AS id, " +
            "       cf.configurationId AS configurationId,  " +
            "       f.description AS description,  " +
            "       f.devicePath AS devicePath,  " +
            "       CASE WHEN f.external THEN f.externalUrl ELSE null END AS externalUrl,  " +
            "       CASE WHEN NOT f.external THEN f.filePath ELSE null END AS filePath,  " +
            "       f.uploadTime AS lastUpdate,  " +
            "       cf.fileId AS fileId,  " +
            "       cf.remove AS remove,  " +
            "       f.replaceVariables AS replaceVariables  " +
            "FROM configurationFiles cf " +
            "    LEFT JOIN uploadedFiles f ON f.id = cf.fileId " +
            "WHERE configurationId = #{configurationId} ORDER BY id")
    List<ConfigurationFile> getConfigurationFiles(@Param("configurationId") int configurationId);

    @Select("SELECT * FROM configurationFiles WHERE configurationId = #{configurationId} AND devicepath = #{path} LIMIT 1")
    @Deprecated
    ConfigurationFile getConfigurationFileByPath(@Param("configurationId") int configurationId, @Param("path") String path);

    @Select("SELECT COUNT(*) AS cnt " +
            "FROM configurationFiles cf " +
            "WHERE cf.fileId = #{fileId}")
    long countFileUsedAsConfigFile(@Param("fileId") Integer fileId);

    @Insert("INSERT INTO configurationFiles(configurationId, fileId, lastUpdate, remove) VALUES " +
            "(#{configurationId}, #{fileId}, 0, #{remove})")
    void insertConfigurationFile(ConfigurationFile file);

    // Used only in the migration task from old to new method of storing configuration files
    @Update("UPDATE configurationFiles SET configurationId=#{configurationId}, description=#{description}, devicePath=#{devicePath}, " +
            "externalUrl=#{externalUrl}, filePath=#{filePath}, checksum=#{checksum}, remove=#{remove}, lastUpdate=#{lastUpdate}, " +
            "fileId=#{fileId}, replaceVariables=#{replaceVariables} WHERE id=#{id}")
    @Deprecated
    void updateOldStyleConfigurationFile(ConfigurationFile file);

    @Select("SELECT c.name " +
            "FROM configurationFiles cf " +
            "INNER JOIN configurations c ON c.id = cf.configurationId " +
            "WHERE cf.fileId = #{fileId}")
    List<String> getUsingConfigurations(@Param("customerId") int customerId, @Param("fileId") Integer fileId);

    @Select({"SELECT configurationFiles.id             AS id, " +
            "       configurations.id                  AS configurationId, " +
            "       configurations.name                AS configurationName, " +
            "       configurations.customerId          AS customerId, " +
            "       uploadedFiles.id                   AS fileId, " +
            "       configurationFiles.id IS NOT NULL  AS upload " +
            "FROM configurations " +
            "         INNER JOIN users ON users.id = #{userId} " +
            "         LEFT JOIN userConfigurationAccess access ON configurations.id = access.configurationId AND access.userId = users.id " +
            "         LEFT JOIN uploadedFiles ON uploadedFiles.id = #{id} " +
            "         LEFT JOIN configurationFiles ON configurations.id = configurationFiles.configurationId AND " +
            "                                         uploadedFiles.id = configurationFiles.fileId " +
            "WHERE configurations.customerId = #{customerId} " +
            "AND (users.allConfigAvailable = TRUE OR NOT access.id IS NULL) " +
            "ORDER BY LOWER(configurations.name)"})
    List<FileConfigurationLink> getConfigurationFileLinks(@Param("customerId") Integer customerId,
                                                          @Param("userId") Integer userId,
                                                          @Param("id") Integer fileId);


    @Delete("DELETE FROM configurationFiles WHERE id = #{id}")
    void deleteConfigurationFile(@Param("id") Integer fileId);
}
