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
import com.hmdm.persistence.domain.ApplicationType;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.ConfigurationFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>A wrapper around the {@link Configuration} object providing the view suitable for the <code>Device List</code>
 * view of server application.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"configuration"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "An MDM configuration used on mobile device")
public class ConfigurationView implements Serializable {

    private static final long serialVersionUID = 3343804830704098674L;
    
    /**
     * <p>A wrapped configuration object.</p>
     */
    private final Configuration configuration;

    /**
     * <p>A list of wrappers around the applications set for configuration.</p>
     */
    private final List<ApplicationView> applications;

    private final List<ConfigurationFileView> files;

    /**
     * <p>Constructs new <code>ConfigurationView</code> instance. This implementation does nothing.</p>
     */
    ConfigurationView(Configuration configuration) {
        this.configuration = configuration;
        this.applications = Optional.ofNullable(configuration.getApplications())
                .map(apps -> apps.stream()
                        .filter(app -> app.getType().equals(ApplicationType.app))         // Check only real apps
                        .map(ApplicationView::new)
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
        this.files = Optional.ofNullable(configuration.getFiles())
                .map(apps -> apps.stream()
                        .map(ConfigurationFileView::new)
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }

    @ApiModelProperty("A configuration ID")
    public Integer getId() {
        return configuration.getId();
    }

    @ApiModelProperty("A unique name of configuration")
    public String getName() {
        return configuration.getName();
    }

    @ApiModelProperty("QR code to enroll the configuration")
    public String getQrCodeKey() {
        return configuration.getQrCodeKey();
    }

    @ApiModelProperty(hidden = true)
    public String getBaseUrl() {
        return configuration.getBaseUrl();
    }

    @ApiModelProperty("A list of applications set and available for for configuration")
    public List<ApplicationView> getApplications() {
        return this.applications;
    }

    @ApiModelProperty("A list of configrration files to be set on device")
    public List<ConfigurationFileView> getFiles() {
        return files;
    }
}
