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
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

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

    @Select("SELECT icons.* FROM icons WHERE icons.id = #{iconId}")
    Icon getIconById(@Param("iconId") int iconId);

    @Select("SELECT icons.* FROM icons WHERE icons.customerId = #{customerId} ORDER BY icons.name")
    List<Icon> getAllIcons(@Param("customerId") int customerId);

    @Select("SELECT COUNT(*) AS cnt " +
            "FROM icons ic " +
            "INNER JOIN uploadedFiles uf ON uf.id = ic.fileId " +
            "WHERE uf.filePath = #{fileName}")
    long countFileUsedAsIcon(@Param("fileName") String fileName);

    @Select("SELECT ic.name " +
            "FROM icons ic " +
            "INNER JOIN uploadedFiles uf ON uf.id = ic.fileId " +
            "WHERE ic.customerId = #{customerId} " +
            "AND uf.filePath = #{fileName}")
    List<String> getUsingIcons(@Param("customerId") int customerId, @Param("fileName") String fileName);
}
