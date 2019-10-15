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

package com.hmdm.guice;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.google.inject.internal.util.$ImmutableSet;
import com.google.inject.servlet.GuiceServletContextListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.inject.spi.Message;
import com.hmdm.guice.module.ConfigureModule;
import com.hmdm.guice.module.LiquibaseModule;
import com.hmdm.guice.module.PersistenceModule;
import com.hmdm.guice.module.PrivateRestModule;
import com.hmdm.guice.module.PublicRestModule;
import com.hmdm.notification.guice.module.NotificationLiquibaseModule;
import com.hmdm.notification.guice.module.NotificationPersistenceModule;
import com.hmdm.notification.guice.module.NotificationRestModule;
import com.hmdm.notification.guice.module.NotificationTaskModule;
import com.hmdm.plugin.PluginConfiguration;
import com.hmdm.plugin.PluginList;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugin.guice.module.PluginLiquibaseModule;
import com.hmdm.plugin.guice.module.PluginPersistenceModule;
import com.hmdm.plugin.guice.module.PluginRestModule;
import com.hmdm.swagger.SwaggerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Initializer extends GuiceServletContextListener {
    private ServletContext context;
    private Injector injector;
    private List<Class<? extends PluginTaskModule>> pluginTaskModules;
    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    public Initializer() {
    }

    protected Injector getInjector() {
        boolean success = false;

        final StringWriter errorOut = new StringWriter();
        PrintWriter errorWriter = new PrintWriter(errorOut);
        try {
            this.injector = Guice.createInjector(Stage.PRODUCTION, this.getModules());
            success = true;
        } catch (ProvisionException e){
            handleException(e, errorWriter);
        } catch (CreationException e){
            handleException(e, errorWriter);
        } catch (Exception e){
            handleException(e, errorWriter);
        }
        if (success) {
            log.debug("Application initialization was successful");
            onInitializationCompletion(null);
        } else {
            log.debug("Application initialization has failed");
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
            File signalFile  = new File(signalFilePath);
            if (!signalFile.exists()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(signalFile))) {
                    if (errorOut == null) {
                        pw.print("OK");
                    } else {
                        pw.print(errorOut.toString());
                    }
                    log.info("Created a signal file for application initialization completion: {}",
                            signalFile.getAbsolutePath());
                } catch (IOException e) {
                    log.error("Failed to create and write to signal file '{}' for application initialization completion",
                            signalFile.getAbsolutePath(), e);
                }
            } else {
                log.warn("The signal file for application initialization completion already exists: {}",
                        signalFile.getAbsolutePath());
            }
        } else {
            log.warn("Could not find 'initialization.completion.signal.file' parameter in context. Signaling on " +
                    "application initialization completion will be skipped.");
        }
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        this.context = servletContextEvent.getServletContext();

        String log4jConfig = context.getInitParameter("log4j.config");
        if (log4jConfig != null && !log4jConfig.isEmpty()) {
            log.info("[LOGGING] Using log4j configuration from: " + log4jConfig);
            System.setProperty("log4j.configuration", log4jConfig);
        } else {
            log.info("[LOGGING] Using log4j configuration from build");
        }

        super.contextInitialized(servletContextEvent);

        initTasks();
    }

    private List<Module> getModules() {
        List<Module> modules = new LinkedList<>();
        modules.add(new PersistenceModule(this.context));
        modules.add(new LiquibaseModule(this.context));
        modules.add(new ConfigureModule(this.context));
        modules.add(new PublicRestModule());
        modules.add(new PrivateRestModule());
        modules.add(new NotificationPersistenceModule(this.context));
        modules.add(new NotificationLiquibaseModule(this.context));
        modules.add(new NotificationRestModule());
        modules.add(new PluginPersistenceModule(this.context));
        modules.add(new PluginLiquibaseModule(this.context));
        modules.add(new PluginRestModule());
        modules.add(new SwaggerModule());

        modules.addAll(getPlugins());

        return modules;
    }

    private void initTasks() {
        final NotificationTaskModule notificationTaskModule = this.injector.getInstance(NotificationTaskModule.class);
        notificationTaskModule.init();

        if (this.pluginTaskModules != null) {
            this.pluginTaskModules.forEach(clazz -> {
                try {
                    final PluginTaskModule pluginTaskModule = this.injector.getInstance(clazz);
                    pluginTaskModule.init();
                } catch (Exception e) {
                    System.err.println("Failed to instantiate and initialize plugin task module '" + clazz.getName() + "'");
                    e.printStackTrace();
                }
            });
        }
    }

    private List<Module> getPlugins() {
        List<Module> result = new ArrayList<>();

        Set<String> processedPlugins = new HashSet<>();

        String pluginsParam = this.context.getInitParameter("plugins");
        if (pluginsParam != null && !pluginsParam.trim().isEmpty()) {
            List<Class<? extends PluginTaskModule>> pluginTaskModules = new ArrayList<>();
            
            String[] plugins = pluginsParam.split(",");
            for (String pluginConfigClassName : plugins) {
                pluginConfigClassName = pluginConfigClassName.trim();
                try {
                    PluginConfiguration pluginConfiguration
                            = (PluginConfiguration) Class.forName(pluginConfigClassName).newInstance();
                    String pluginId = pluginConfiguration.getPluginId().toLowerCase();
                    if (processedPlugins.contains(pluginId)) {
                        System.err.println("Duplicate plugin found: " + pluginId + ". Skipping initialization of "
                                + pluginConfiguration.getClass().getName());
                        continue;
                    }
                    processedPlugins.add(pluginId);
                    PluginList.enablePlugin(pluginId);

                    List<Module> pluginModules = pluginConfiguration.getPluginModules(context);
                    if (pluginModules != null && !pluginModules.isEmpty()) {
                        result.addAll(pluginModules);
                    }

                    pluginConfiguration.getTaskModules(this.context).ifPresent(pluginTaskModules::addAll);

                } catch (InstantiationException | IllegalAccessException e) {
                    System.err.println("Failed to instantiate plugin configuration for plugin '" + pluginConfigClassName + "'");
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    System.err.println("Could not find plugin configuration class: " + pluginConfigClassName);
                }
            }

            this.pluginTaskModules = pluginTaskModules;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void handleException(ProvisionException e, PrintWriter errorWriter) {
        try {
            Field messagesField = ProvisionException.class.getDeclaredField("messages");
            messagesField.setAccessible(true);
            $ImmutableSet<Message> messages = ($ImmutableSet<Message>) messagesField.get(e);
            messages.iterator().forEachRemaining(message -> log.error("[INITIALIZER]: ERROR: " + message.getMessage()));
            messages.iterator().forEachRemaining(message -> errorWriter.println("[INITIALIZER]: ERROR: " + message.getMessage()));
            messagesField.setAccessible(false);
        } catch (Exception e1) {
            log.error("Failed to access [messages] field: " + e1);
        }
    }

    private void handleException(Exception e, PrintWriter errorWriter) {
        try {
            log.error("[INITIALIZER] : ERROR : ");
            e.printStackTrace();
            e.printStackTrace(errorWriter);
        } catch (Exception e1) {
            if (e.getCause() != null) {
                log.error("[INITIALIZER] : ERROR : " + e.getCause().getMessage());
                errorWriter.println("[INITIALIZER] : ERROR : " + e.getCause().getMessage());
                e.getCause().printStackTrace();
                e.getCause().printStackTrace(errorWriter);
            } else {
                log.error("[INITIALIZER] : ERROR OF TYPE : " + e.getClass().getName());
                errorWriter.println("[INITIALIZER] : ERROR OF TYPE : " + e.getClass().getName());
            }
        }
    }

    private void handleException(CreationException e, PrintWriter errorWriter) {
        try {
            e.getErrorMessages().forEach(m-> log.error("[INITIALIZER] : ERROR : " +  m.getMessage()));
            e.getErrorMessages().forEach(m-> errorWriter.println(m.getMessage()));
            e.printStackTrace(errorWriter);
        } catch (Exception e1) {
            if (e.getCause() != null) {
                log.error("[INITIALIZER] : ERROR : " + e.getCause().getMessage());
                errorWriter.println(e.getCause().getMessage());
                e.getCause().printStackTrace();
                e.getCause().printStackTrace(errorWriter);
            } else {
                log.error("[INITIALIZER] : ERROR OF TYPE : " + e.getClass().getName());
                errorWriter.println("[INITIALIZER] : ERROR OF TYPE : " + e.getClass().getName());
            }
        }
    }
}
