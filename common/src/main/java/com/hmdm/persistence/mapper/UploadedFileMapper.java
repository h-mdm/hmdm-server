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
import com.hmdm.rest.json.FileConfigurationLink;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>An ORM mapper for {@link UploadedFile} domain objects.</p>
 *
 * @author isv
 */
public interface UploadedFileMapper {

    @Insert("INSERT INTO uploadedFiles (customerId, filePath, description, uploadTime, devicePath, " +
            "external, externalUrl, replaceVariables) " +
            "VALUES (#{customerId}, #{filePath}, #{description}, #{uploadTime}, #{devicePath}, " +
            "#{external}, #{externalUrl}, #{replaceVariables})")
    @SelectKey(statement = "SELECT currval('uploadedfiles_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class)
    int insert(UploadedFile file);

    @Update({"UPDATE uploadedFiles " +
            "SET filePath = #{filePath}, description=#{description}, uploadTime=#{uploadTime}, " +
            "devicePath=#{devicePath}, external=#{external}, externalUrl=#{externalUrl}, replaceVariables=#{replaceVariables} " +
            "WHERE id=#{id}"})
    void update(UploadedFile file);


    @Select("SELECT * FROM uploadedFiles " +
            "WHERE customerId = #{customerId} " +
            "ORDER BY filePath")
    List<UploadedFile> getAll(@Param("customerId") int customerId);

    @Select("SELECT * FROM uploadedFiles " +
            "WHERE customerId = #{customerId} AND (filePath ILIKE #{value} OR description ILIKE #{value} OR externalUrl ILIKE #{value}) " +
            "ORDER BY filePath")
    List<UploadedFile> getAllByValue(@Param("customerId") int customerId,
                              @Param("value") String value);

    @Select("SELECT * FROM uploadedFiles WHERE id = #{id}")
    UploadedFile findById(@Param("id") Integer id);

    @Select("SELECT * FROM uploadedFiles WHERE customerId = #{customerId} AND filePath = #{filePath} LIMIT 1")
    UploadedFile findByPath(@Param("customerId") Integer customerId, @Param("filePath") String filePath);

    @Select("SELECT * FROM uploadedFiles WHERE customerId = #{customerId} AND " +
            "    description IS NOT DISTINCT FROM #{description} AND " +
            "    filePath IS NOT DISTINCT FROM #{filePath} AND " +
            "    devicePath IS NOT DISTINCT FROM #{devicePath} AND " +
            "    external=#{external} AND " +
            "    externalUrl IS NOT DISTINCT FROM #{externalUrl} AND " +
            "    replaceVariables=#{replaceVariables}")
    UploadedFile findSame(@Param("customerId") Integer customerId,
                          @Param("description") String description,
                          @Param("filePath") String filePath,
                          @Param("devicePath") String devicePath,
                          @Param("external") boolean external,
                          @Param("externalUrl") String externalUrl,
                          @Param("replaceVariables") boolean replaceVariables);

    @Delete("DELETE FROM uploadedFiles WHERE id = #{id}")
    void delete(@Param("id") int fileId);

    @Select("SELECT f.* FROM uploadedFiles f LEFT JOIN configurationFiles cf ON cf.fileId = f.id " +
            "WHERE cf.fileId IS NULL AND f.customerId=#{customerId}")
    List<UploadedFile> findOrphaned(@Param("customerId") Integer customerId);

    @Select("SELECT COUNT(*) AS cnt " +
            "FROM uploadedFiles " +
            "WHERE NOT external AND filePath=#{filePath} AND id != #{id} AND customerId=#{customerId}")
    long countUploadedDuplicates(@Param("id") Integer id,
                                 @Param("customerId") Integer customerId,
                                 @Param("filePath") String filePath);

    @Select("SELECT COUNT(*) AS cnt " +
            "FROM uploadedFiles " +
            "WHERE external AND externalUrl=#{externalUrl} AND id != #{id} AND customerId=#{customerId}")
    long countExternalDuplicates(@Param("id") Integer id,
                                 @Param("customerId") Integer customerId,
                                 @Param("externalUrl") String externalUrl);
}
