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

package com.hmdm.plugin.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.User;
import com.hmdm.plugin.PluginList;
import com.hmdm.plugin.persistence.domain.DisabledPlugin;
import com.hmdm.plugin.persistence.domain.Plugin;
import com.hmdm.plugin.persistence.mapper.PluginMapper;
import com.hmdm.security.SecurityContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * <p>$</p>
 *
 * @author isv
 */
@Singleton
public class PluginStatusCache {

    /**
     * <p>An ORM mapper for domain object type.</p>
     */
    private final PluginMapper pluginMapper;

    private final Map<String, Plugin> pluginsByIdentifiers;
    private final Map<Integer, Set<Integer>> customerDisabledPlugins;

    /**
     * <p>Constructs new <code>PluginStatusCache</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PluginStatusCache(PluginMapper pluginMapper) {
        this.pluginMapper = pluginMapper;

        final List<Plugin> registeredPlugins = this.pluginMapper.findRegisteredPlugins()
                .stream()
                .filter(p -> PluginList.isPluginEnabled(p.getIdentifier()))
                .collect(Collectors.toList());

        this.pluginsByIdentifiers = registeredPlugins.stream().collect(Collectors.toMap(Plugin::getIdentifier, p -> p));

        List<DisabledPlugin> disabledPluginsForCustomers = this.pluginMapper.getDisabledPluginsForAllCustomers();
        final Map<Integer, Set<Integer>> customerDisabledPlugins = new ConcurrentSkipListMap<>();
        disabledPluginsForCustomers.forEach(disabledPlugin -> {
            final int customerId = disabledPlugin.getCustomerId();
            if (!customerDisabledPlugins.containsKey(customerId)) {
                customerDisabledPlugins.put(customerId, new ConcurrentSkipListSet<>());
            }
            customerDisabledPlugins.get(customerId).add(disabledPlugin.getPluginId());
        });

        this.customerDisabledPlugins = customerDisabledPlugins;

    }

    public boolean isPluginDisabled(String pluginIdentifier) {
        if (!this.pluginsByIdentifiers.containsKey(pluginIdentifier)) {
            return true;
        }

        final Plugin plugin = this.pluginsByIdentifiers.get(pluginIdentifier);

        if (SecurityContext.get() != null && SecurityContext.get().getCurrentCustomerId().isPresent()) {
            final int customerId = SecurityContext.get().getCurrentCustomerId().get();
            if (!this.customerDisabledPlugins.containsKey(customerId)) {
                final Set<Integer> customerDisabledPlugins = this.pluginMapper.getDisabledPluginsForCustomer(customerId)
                        .stream()
                        .map(DisabledPlugin::getPluginId)
                        .collect(Collectors.toSet());
                this.customerDisabledPlugins.put(customerId, customerDisabledPlugins);
            }

            if (this.customerDisabledPlugins.get(customerId).contains(plugin.getId())) {
                return true;
            }
        } else {
            // TODO : Need to check the potential anonymous calls from devices to plugin endpoints by getting the device number from request and mapping it to customer
        }


        return false;
    }

    public void setCustomerDisabledPlugins(int customerId, Integer[] pluginIds) {
        this.customerDisabledPlugins.put(customerId, new ConcurrentSkipListSet<>(Arrays.asList(pluginIds)));
    }
}
