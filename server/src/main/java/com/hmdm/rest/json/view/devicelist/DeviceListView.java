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

package com.hmdm.rest.json.view.devicelist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.rest.json.PaginatedData;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <pA DTO carrying the details for <code>Device List</code> view.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "The list of devices with configurations lookup")
public class DeviceListView implements Serializable {

    /**
     * <p>A mapping from configuration IDs for configurations data. Lists the configurations referenced from the
     * provided devices.</p>
     */
    private final Map<Integer, ConfigurationView> configurations;

    /**
     * <p>A list of devices.</p>
     */
    private final PaginatedData<DeviceView> devices;

    /**
     * <p>Constructs new <code>DeviceListView</code> instance. This implementation does nothing.</p>
     */
    public DeviceListView(@NotNull Collection<Configuration> configurations,
                          @NotNull PaginatedData<DeviceView> devices) {
        this.configurations = configurations.stream()
                .map(ConfigurationView::new)
                .collect(Collectors.toMap(ConfigurationView::getId, c -> c));
        this.devices = devices;
    }

    public Map<Integer, ConfigurationView> getConfigurations() {
        return configurations;
    }

    public PaginatedData<DeviceView> getDevices() {
        return devices;
    }
}
