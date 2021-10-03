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

package com.hmdm.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.hmdm.persistence.domain.Application;

import javax.servlet.ServletContext;

public class ConfigureModule extends AbstractModule {
    private final String filesDirectoryParameter = "files.directory";
    private final String baseUrlParameter = "base.url";
    private final String pluginFilesDirectoryParameter = "plugins.files.directory";
    private final String usageScenarioParameter = "usage.scenario";
    private final String secureEnrollmentParameter = "secure.enrollment";
    private final String hashSecretParameter = "hash.secret";
    private final String aaptCommandParameter = "aapt.command";
    private final String roleOrgadminIdParameter = "role.orgadmin.id";
    private final String launcherPackageParameter = "launcher.package";
    private final String rebrandingNameParameter = "rebranding.name";
    private final String rebrandingVendorParameter = "rebranding.vendor.name";
    private final String rebrandingVendorLinkParameter = "rebranding.vendor.link";
    private final String rebrandingLogoParameter = "rebranding.logo";
    private final String rebrandingMobileNameParameter = "rebranding.mobile.name";
    private final ServletContext context;

    public ConfigureModule(ServletContext context) {
        this.context = context;
    }

    protected void configure() {
        this.bindConstant().annotatedWith(Names.named(filesDirectoryParameter)).to(this.context.getInitParameter(filesDirectoryParameter));
        this.bindConstant().annotatedWith(Names.named(baseUrlParameter)).to(this.context.getInitParameter(baseUrlParameter));
        this.bindConstant().annotatedWith(Names.named(pluginFilesDirectoryParameter)).to(this.context.getInitParameter(pluginFilesDirectoryParameter));
        this.bindConstant().annotatedWith(Names.named(usageScenarioParameter)).to(this.context.getInitParameter(usageScenarioParameter));
        String secureEnrollment = this.context.getInitParameter(secureEnrollmentParameter);
        this.bindConstant().annotatedWith(Names.named(secureEnrollmentParameter)).to(
                secureEnrollment != null && (secureEnrollment.equals("1") || secureEnrollment.equalsIgnoreCase("true"))
        );
        this.bindConstant().annotatedWith(Names.named(hashSecretParameter)).to(this.context.getInitParameter(hashSecretParameter));
        this.bindConstant().annotatedWith(Names.named(aaptCommandParameter)).to(this.context.getInitParameter(aaptCommandParameter));
        this.bindConstant().annotatedWith(Names.named(roleOrgadminIdParameter)).to(
                Integer.parseInt(this.context.getInitParameter(roleOrgadminIdParameter))
        );
        String launcherPackage = this.context.getInitParameter(launcherPackageParameter);
        if (launcherPackage == null) {
            launcherPackage = Application.DEFAULT_LAUNCHER_PACKAGE;
        }
        this.bindConstant().annotatedWith(Names.named(launcherPackageParameter)).to(launcherPackage);

        // Optional parameters (Guice doesn't allow to bind string to null)
        String opt;
        opt = this.context.getInitParameter(rebrandingNameParameter);
        this.bindConstant().annotatedWith(Names.named(rebrandingNameParameter)).to(opt != null ? opt : "");
        opt = this.context.getInitParameter(rebrandingVendorParameter);
        this.bindConstant().annotatedWith(Names.named(rebrandingVendorParameter)).to(opt != null ? opt : "");
        opt = this.context.getInitParameter(rebrandingVendorLinkParameter);
        this.bindConstant().annotatedWith(Names.named(rebrandingVendorLinkParameter)).to(opt != null ? opt : "");
        opt = this.context.getInitParameter(rebrandingLogoParameter);
        this.bindConstant().annotatedWith(Names.named(rebrandingLogoParameter)).to(opt != null ? opt : "");
        opt = this.context.getInitParameter(rebrandingMobileNameParameter);
        this.bindConstant().annotatedWith(Names.named(rebrandingMobileNameParameter)).to(opt != null ? opt : "");
    }
}