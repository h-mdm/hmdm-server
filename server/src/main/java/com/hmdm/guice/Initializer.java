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
import com.google.inject.internal.util.$ImmutableSet;
import com.google.inject.servlet.GuiceServletContextListener;

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
import com.hmdm.guice.module.InitializationCompletionSignalingModule;
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

public final class Initializer extends GuiceServletContextListener {
    private ServletContext context;
    private Injector injector;
    private List<Class<? extends PluginTaskModule>> pluginTaskModules;

    public Initializer() {
    }

    protected Injector getInjector() {
        try {
            this.injector = Guice.createInjector(this.getModules());
        } catch (ProvisionException e){
            handleException(e);
        } catch (CreationException e){
            handleException(e);
        } catch (Exception e){
            handleException(e);
        }
        return injector;
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        this.context = servletContextEvent.getServletContext();
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
        
        modules.add(new InitializationCompletionSignalingModule(this.context));

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
    private void handleException(ProvisionException e) {
        try {
            Field messagesField = ProvisionException.class.getDeclaredField("messages");
            messagesField.setAccessible(true);
            $ImmutableSet<Message> messages = ($ImmutableSet<Message>) messagesField.get(e);
            messages.iterator().forEachRemaining(message -> System.out.println("[INITIALIZER]: ERROR: " + message.getMessage()));
            messagesField.setAccessible(false);
        } catch (Exception e1) {
            System.out.println("Failed to access [messages] field: " + e1);
        }
    }

    private void handleException(Exception e) {
        try {
            System.out.println("[INITIALIZER] : ERROR : ");
            e.printStackTrace();
        } catch (Exception e1) {
            if (e.getCause() != null) {
                System.out.println("[INITIALIZER] : ERROR : " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            } else {
                System.out.println("[INITIALIZER] : ERROR OF TYPE : " + e.getClass().getName());
            }
        }
    }

    private void handleException(CreationException e) {
        try {
            e.getErrorMessages().forEach(m-> System.out.println("[INITIALIZER] : ERROR : " +  m.getMessage()));
        } catch (Exception e1) {
            if (e.getCause() != null) {
                System.out.println("[INITIALIZER] : ERROR : " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            } else {
                System.out.println("[INITIALIZER] : ERROR OF TYPE : " + e.getClass().getName());
            }
        }
    }
}
