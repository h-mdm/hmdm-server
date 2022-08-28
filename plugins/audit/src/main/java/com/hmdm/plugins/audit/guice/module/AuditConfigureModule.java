package com.hmdm.plugins.audit.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import javax.servlet.ServletContext;

public class AuditConfigureModule extends AbstractModule {
    private final ServletContext context;

    public AuditConfigureModule(ServletContext context) {
        this.context = context;
    }

    protected void configure() {
        String displayForwardedIp = "plugin.audit.display.forwarded.ip";
        String displayForwardedIpTag = this.context.getInitParameter(displayForwardedIp);
        this.bindConstant().annotatedWith(Names.named(displayForwardedIp)).to(
                displayForwardedIpTag != null && (displayForwardedIpTag.equals("1") || displayForwardedIpTag.equalsIgnoreCase("true"))
        );
    }
}
