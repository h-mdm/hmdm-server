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

package com.hmdm.plugin.persistence.mapper;

import com.hmdm.plugin.persistence.domain.DisabledPlugin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.hmdm.plugin.persistence.domain.Plugin;

import java.util.List;

/**
 * <p>An ORM mapper for {@link Plugin} domain object.</p>
 *
 * @author isv
 */
public interface PluginMapper {

    @Select({"SELECT plugins.* " +
            "FROM plugins " +
            "WHERE disabled = FALSE " +
            "AND NOT EXISTS (SELECT 1 FROM pluginsDisabled " +
            "                WHERE pluginsDisabled.customerId=#{customerId} " +
            "                AND pluginsDisabled.pluginId=plugins.id)"})
    List<Plugin> findAvailablePluginsByCustomerId(@Param("customerId") int customerId);

    @Select("SELECT plugins.* FROM plugins WHERE disabled = FALSE ORDER BY plugins.identifier")
    List<Plugin> findActivePlugins();

    @Select("SELECT plugins.* FROM plugins ORDER BY plugins.identifier")
    List<Plugin> findRegisteredPlugins();

    @Delete("DELETE FROM pluginsDisabled WHERE customerId = #{customerId}")
    int cleanUpDisabledPlugins(@Param("customerId") int customerId);

    int insertDisabledPlugin(@Param("pluginIds") Integer[] pluginIds, @Param("customerId") int customerId);

    @Select("SELECT customerId, pluginId FROM pluginsDisabled ORDER BY customerId, pluginId")
    List<DisabledPlugin> getDisabledPluginsForAllCustomers();

    @Select("SELECT customerId, pluginId FROM pluginsDisabled WHERE customerId = #{customerId} ORDER BY customerId, pluginId")
    List<DisabledPlugin> getDisabledPluginsForCustomer(@Param("customerId") int customerId);
}
