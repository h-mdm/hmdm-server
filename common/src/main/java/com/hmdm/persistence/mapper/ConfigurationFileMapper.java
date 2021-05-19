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
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>An ORM mapper for the {@link com.hmdm.persistence.domain.ConfigurationFile} objects.</p>
 */
public interface ConfigurationFileMapper {

    @Select("SELECT * FROM configurationFiles WHERE configurationId = #{configurationId} ORDER BY id")
    List<ConfigurationFile> getConfigurationFiles(@Param("configurationId") int configurationId);

    @Select("SELECT * FROM configurationFiles WHERE configurationId = #{configurationId} AND devicepath = #{path} LIMIT 1")
    ConfigurationFile getConfigurationFileByPath(@Param("configurationId") int configurationId, @Param("path") String path);

    @Select("SELECT COUNT(*) AS cnt " +
            "FROM configurationFiles cf " +
            "INNER JOIN uploadedFiles uf ON uf.id = cf.fileId " +
            "WHERE uf.filePath = #{fileName}")
    long countFileUsedAsConfigFile(@Param("fileName") String fileName);

    @Insert("INSERT INTO configurationFiles(configurationId, description, devicePath, externalUrl, " +
            "filePath, checksum, remove, lastUpdate, fileId, replaceVariables) VALUES " +
            "(#{configurationId}, #{description}, #{devicePath}, #{externalUrl}, #{filePath}, #{checksum}, " +
            "#{remove}, #{lastUpdate}, #{fileId}, #{replaceVariables})")
    void insertConfigurationFile(ConfigurationFile file);

    @Update("UPDATE configurationFiles SET configurationId=#{configurationId}, description=#{description}, devicePath=#{devicePath}, " +
            "externalUrl=#{externalUrl}, filePath=#{filePath}, checksum=#{checksum}, remove=#{remove}, lastUpdate=#{lastUpdate}, " +
            "fileId=#{fileId}, replaceVariables=#{replaceVariables} WHERE id=#{id}")
    void updateConfigurationFile(ConfigurationFile file);

    @Select("SELECT c.name " +
            "FROM configurationFiles cf " +
            "INNER JOIN configurations c ON c.id = cf.configurationId " +
            "INNER JOIN uploadedFiles uf ON uf.id = cf.fileId " +
            "WHERE c.customerId = #{customerId} " +
            "AND uf.filePath = #{fileName}")
    List<String> getUsingConfigurations(@Param("customerId") int customerId, @Param("fileName") String fileName);
}
