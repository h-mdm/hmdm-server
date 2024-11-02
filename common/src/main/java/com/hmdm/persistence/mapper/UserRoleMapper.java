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

import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.domain.UserRolePermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserRoleMapper {

    List<UserRolePermission> getPermissionsList();

    List<UserRole> findAll();

    UserRole findByName(@Param("name") String name);

    UserRole findById(@Param("id") Integer id);

    @Insert({"INSERT INTO userroles (name, description, superadmin) " +
            "VALUES (#{name}, #{description}, false)"})
    @SelectKey( statement = "SELECT currval('userroles_id_seq')", keyColumn = "id", keyProperty = "id",
                before = false, resultType = int.class )
    void insert(UserRole userRole);

    @Update({"UPDATE userroles " +
            "SET name = #{name}, description=#{description}, superadmin=false " +
            "WHERE id=#{id} AND NOT superadmin"})
    void update(UserRole userRole);

    @Delete({"DELETE FROM userroles WHERE id=#{id} AND NOT superadmin"})
    void delete(@Param("id") Integer roleId);

    @Delete({"DELETE FROM userrolepermissions " +
            "WHERE roleid=#{id} "})
    void deletePermissions(@Param("id") Integer roleId);

    void insertPermissions(@Param("id") Integer roleId, @Param("permissions") List<Integer> permissions);
}
