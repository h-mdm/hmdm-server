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

import com.hmdm.persistence.domain.Icon;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>An ORM Mapper for {@link Icon} domain object.</p>
 *
 * @author isv
 */
public interface IconMapper {

    @Insert("INSERT INTO icons (customerId, name, fileId) VALUES (#{customerId}, #{name}, #{fileId})")
    @SelectKey(statement = "SELECT currval('icons_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class)
    int insertIcon(Icon icon);

    @Update("UPDATE icons SET name=#{name}, fileId=#{fileId} WHERE id=#{id} AND customerId=#{customerId}")
    int updateIcon(Icon icon);

    @Select("SELECT icons.* FROM icons WHERE icons.id = #{iconId}")
    Icon getIconById(@Param("iconId") int iconId);

    @Select("SELECT icons.*, CASE " +
            "    WHEN f.description IS NOT NULL AND f.description <> '' " +
            "        THEN f.description " +
            "        ELSE CASE " +
            "                 WHEN f.external THEN f.externalUrl " +
            "                 ELSE f.filePath " +
            "             END " +
            "    END AS fileName FROM icons " +
            "    LEFT JOIN uploadedFiles f ON icons.fileId = f.id " +
            "WHERE icons.customerId = #{customerId} ORDER BY icons.name")
    List<Icon> getAllIcons(@Param("customerId") int customerId);

    @Select("SELECT icons.*, CASE " +
            "    WHEN f.description IS NOT NULL AND f.description <> '' " +
            "        THEN f.description " +
            "        ELSE CASE " +
            "                 WHEN f.external THEN f.externalUrl " +
            "                 ELSE f.filePath " +
            "             END " +
            "    END AS fileName FROM icons " +
            "    LEFT JOIN uploadedFiles f ON icons.fileId = f.id " +
            "WHERE icons.customerId = #{customerId} AND icons.name ILIKE #{value} ORDER BY icons.name")
    List<Icon> getAllIconsByValue(@Param("customerId") int customerId, @Param("value") String value);

    @Delete({"DELETE FROM icons WHERE id = #{id}"})
    void removeById(@Param("id") Integer id);

    @Select({"SELECT * FROM icons WHERE id = #{id}"})
    Icon getById(@Param("id") Integer id);

    @Select("SELECT COUNT(*) AS cnt " +
            "FROM icons ic " +
            "WHERE ic.fileId = #{fileId}")
    long countFileUsedAsIcon(@Param("fileId") Integer fileId);

    @Select("SELECT ic.name " +
            "FROM icons ic " +
            "WHERE ic.fileId = #{fileId}")
    List<String> getUsingIcons(@Param("customerId") int customerId, @Param("fileId") Integer fileId);
}
