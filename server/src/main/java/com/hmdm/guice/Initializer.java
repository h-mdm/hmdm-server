/*
 * Headwind MDM: Open Source Android MDM Software https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (https://h-mdm.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.hmdm.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.hmdm.guice.module.*;
import com.hmdm.notification.guice.module.*;
import com.hmdm.plugin.PluginList;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.plugin.guice.module.PluginLiquibaseModule;
import com.hmdm.plugin.guice.module.PluginPersistenceModule;
import com.hmdm.plugin.guice.module.PluginPlatformTaskModule;
import com.hmdm.plugin.guice.module.PluginRestModule;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public final class Initializer extends GuiceServletContextListener {
    private ServletContext context;
    private Injector injector;
    private final List<Runnable> taskShutdowns = new LinkedList<>();

    public Initializer() {}

    protected Injector getInjector() {
        boolean success = false;

        final StringWriter errorOut = new StringWriter();
        PrintWriter errorWriter = new PrintWriter(errorOut);
        try {
            this.injector = Guice.createInjector(Stage.PRODUCTION, this.getModules());
            taskShutdowns.add(this.injector.getInstance(BackgroundTaskRunnerService.class)::shutdown);
            success = true;
        } catch (Exception e) {
            System.err.println("[HMDM-INITIALIZER]: Unexpected error during injector initialization: " + e);
            e.printStackTrace();
            e.printStackTrace(errorWriter);
        }
        if (success) {
            System.out.println("[HMDM-INITIALIZER]: Application initialization was successful");
            onInitializationCompletion(null);
        } else {
            System.out.println("[HMDM-INITIALIZER]: Application initialization has failed");
            onInitializationCompletion(errorOut);
        }
        return injector;
    }

    /**
     * <p>Signals on application initialization completion.</p>
     */
    private void onInitializationCompletion(StringWriter errorOut) {

        final String signalFilePath = this.context.getInitParameter("initialization.completion.signal.file");
        if (signalFilePath != null && !signalFilePath.trim().isEmpty()) {
            File signalFile = new File(signalFilePath);
            if (!signalFile.exists()) {
                try {
                    FileWriter fw = new FileWriter(signalFile, StandardCharsets.UTF_8);
                    PrintWriter pw = new PrintWriter(fw);
                    if (errorOut == null) {
                        pw.print("OK");
                    } else {
                        pw.print(errorOut.toString());
                    }
                    pw.close();
                    fw.close();
                    System.out.println("[HMDM-INITIALIZER]: Created a signal file for application "
                            + "initialization completion: " + signalFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("[HMDM-INITIALIZER]: Failed to create and write to signal file '"
                            + signalFile.getAbsolutePath() + "' for application initialization completion" + e);
                }
            } else {
                System.out.println("[HMDM-INITIALIZER]: The signal file for application initialization completion "
                        + "already exists: " + signalFile.getAbsolutePath());
            }
        } else {
            System.out.println("Could not find 'initialization.completion.signal.file' parameter in context. "
                    + "Signaling on application initialization completion will be skipped.");
        }
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        this.context = servletContextEvent.getServletContext();

        String logbackConfig = context.getInitParameter("logback.config");
        if (logbackConfig != null && !logbackConfig.isEmpty()) {
            System.out.println("[HMDM-LOGGING] Reconfiguring Logback from: " + logbackConfig);

            org.slf4j.ILoggerFactory loggerFactory = org.slf4j.LoggerFactory.getILoggerFactory();
            if (loggerFactory instanceof ch.qos.logback.classic.LoggerContext loggerContext) {
                // NOTE: deliberate deviation from the upstream (convergent dd0c39a) form of this block.
                // Upstream called loggerContext.reset() before resolving the URL, so any failure -- an
                // unresolved ${logback.config} token, a missing file, malformed XML -- left the logger
                // context wiped with no appenders and silenced the application, while printing a
                // "falling back" message that nothing implemented. The URL is now resolved before
                // anything is reset, and the failure path genuinely restores the bundled configuration.
                // Please do not "restore" this to match upstream.
                java.net.URL configUrl = null;
                try {
                    configUrl = java.net.URI.create(logbackConfig).toURL();
                } catch (Exception e) {
                    System.err.println("[HMDM-LOGGING] Invalid logback.config URL '" + logbackConfig
                            + "': " + e);
                    System.err.println("[HMDM-LOGGING] Keeping the bundled logback.xml configuration");
                }

                if (configUrl != null) {
                    try {
                        loggerContext.reset();

                        ch.qos.logback.classic.joran.JoranConfigurator configurator =
                                new ch.qos.logback.classic.joran.JoranConfigurator();
                        configurator.setContext(loggerContext);
                        configurator.doConfigure(configUrl);

                        System.out.println("[HMDM-LOGGING] Logback reconfigured successfully");
                    } catch (Exception e) {
                        System.err.println("[HMDM-LOGGING] Failed to reconfigure Logback: " + e);
                        restoreBundledLogbackConfig(loggerContext);
                    }
                }
            } else {
                // SLF4J resolved to a provider other than Logback, so there is nothing to
                // reconfigure. Skip it rather than letting a ClassCastException escape
                // contextInitialized() and abort the deployment of the web application.
                System.err.println("[HMDM-LOGGING] SLF4J is bound to "
                        + loggerFactory.getClass().getName() + ", not to Logback; "
                        + "skipping the reconfiguration from " + logbackConfig);
            }
        } else {
            System.out.println("[HMDM-LOGGING] Using bundled Logback configuration");
        }

        super.contextInitialized(servletContextEvent);

        initTasks();
    }

    /**
     * <p>Re-applies the <code>logback.xml</code> bundled in the web application after an attempt to apply an external configuration has failed.</p>
     *
     * <p>The context is reset first: a <code>doConfigure()</code> that threw part-way through may have left a partially applied configuration behind.
     * Without this, a failed reconfiguration leaves the logger context with no appenders at all and the application logs nothing.</p>
     *
     * @param loggerContext the Logback context to restore.
     */
    private static void restoreBundledLogbackConfig(ch.qos.logback.classic.LoggerContext loggerContext) {
        try {
            loggerContext.reset();
            new ch.qos.logback.classic.util.ContextInitializer(loggerContext)
                    .autoConfig(Initializer.class.getClassLoader());
            System.err.println("[HMDM-LOGGING] Restored the bundled logback.xml configuration");
        } catch (Exception e) {
            System.err.println("[HMDM-LOGGING] Could not restore the bundled logback.xml "
                    + "configuration, logging may be disabled: " + e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // Retain the exact instances started by this context; Guice lookups here can
        // otherwise create new services while the old workers continue running.
        for (Runnable shutdown : taskShutdowns.reversed()) {
            try {
                shutdown.run();
            } catch (Exception e) {
                System.err.println("[HMDM-INITIALIZER]: Error shutting down application task: " + e);
            }
        }
        taskShutdowns.clear();
        super.contextDestroyed(servletContextEvent);
    }

    private List<Module> getModules() {
        List<Module> modules = new LinkedList<>();
        modules.add(new PersistenceModule(this.context));
        modules.add(new LiquibaseModule(this.context));
        modules.add(new ConfigureModule(this.context));
        modules.add(new MainRestModule());
        modules.add(new PublicRestModule());
        modules.add(new PrivateRestModule());
        modules.add(new NotificationPersistenceModule(this.context));
        modules.add(new NotificationLiquibaseModule(this.context));
        modules.add(new NotificationRestModule());
        modules.add(new NotificationEngineSelectorModule());
        modules.add(new NotificationMqttConfigModule(this.context));
        modules.add(new PluginPersistenceModule(this.context));
        modules.add(new PluginLiquibaseModule(this.context));
        modules.add(new PluginRestModule());

        PluginList.init(this.context);

        modules.addAll(PluginList.getPluginModules());

        return modules;
    }

    private void initTasks() {
        final NotificationTaskModule notificationTaskModule = this.injector.getInstance(NotificationTaskModule.class);
        taskShutdowns.add(notificationTaskModule::shutdown);
        notificationTaskModule.init();

        final NotificationMqttTaskModule notificationMqttTaskModule =
                this.injector.getInstance(NotificationMqttTaskModule.class);
        taskShutdowns.add(notificationMqttTaskModule::shutdown);
        notificationMqttTaskModule.init();

        final PluginPlatformTaskModule pluginPlatformTaskModule =
                this.injector.getInstance(PluginPlatformTaskModule.class);
        pluginPlatformTaskModule.init();

        final List<Class<? extends PluginTaskModule>> pluginTaskModules = PluginList.getPluginTaskModules();
        if (pluginTaskModules != null) {
            pluginTaskModules.forEach(clazz -> {
                try {
                    final PluginTaskModule pluginTaskModule = this.injector.getInstance(clazz);
                    pluginTaskModule.init();
                } catch (Exception e) {
                    System.err.println(
                            "Failed to instantiate and initialize plugin task module '" + clazz.getName() + "': " + e);
                }
            });
        }

        final EventListenerModule eventListenerModule = this.injector.getInstance(EventListenerModule.class);
        taskShutdowns.add(eventListenerModule::shutdown);
        eventListenerModule.init();

        final StartupTaskModule startupTaskModule = this.injector.getInstance(StartupTaskModule.class);
        startupTaskModule.init();
    }
}
