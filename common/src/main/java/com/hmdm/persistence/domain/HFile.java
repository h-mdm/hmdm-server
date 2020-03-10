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

package com.hmdm.persistence.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@ApiModel(description = "A single file maintained by the MDM server")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HFile implements Comparable<HFile>, Serializable {

    private static final long serialVersionUID = 7570897379289300175L;
    
    @ApiModelProperty("A path to a file")
    private String path;
    @ApiModelProperty("A name of file")
    private String name;
    @ApiModelProperty("An URL of file")
    private String url;

    private List<String> usedByApps;
    private List<String> usedByIcons;
    private List<String> usedByConfigurations;

    public HFile() {
    }

    public HFile(String path, String name, String url) {
        this.path = path;
        if (path.length() == 0) {
            this.path = "/";
        }

        this.name = name;
        this.url = url;

    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getUsedByApps() {
        return usedByApps;
    }

    public void setUsedByApps(List<String> usedByApps) {
        this.usedByApps = usedByApps;
    }

    public List<String> getUsedByIcons() {
        return usedByIcons;
    }

    public void setUsedByIcons(List<String> usedByIcons) {
        this.usedByIcons = usedByIcons;
    }

    public List<String> getUsedByConfigurations() {
        return usedByConfigurations;
    }

    public void setUsedByConfigurations(List<String> usedByConfigurations) {
        this.usedByConfigurations = usedByConfigurations;
    }

    /**
     * <p>Compares this file to another one based on path and filename.</p>
     */
    @Override
    public int compareTo(HFile o) {
        if (this == o) {
            return 0;
        } else if (o == null) {
            return 1;
        } else {
            final String thisPath = this.getPath() == null ? "" : this.getPath();
            final String otherPath = o.getPath() == null ? "" : o.getPath();

            final int pathComparisonResult = thisPath.compareTo(otherPath);
            if (pathComparisonResult == 0) {
                final String thisName = this.getName() == null ? "" : this.getName().toLowerCase();
                final String otherName = o.getName() == null ? "" : o.getName().toLowerCase();

                return thisName.compareTo(otherName);
            } else {
                return pathComparisonResult;
            }
        }
    }
}
