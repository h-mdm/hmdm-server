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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>A module to be put as last into application modules chain to signal on application initialization completion. This
 * module creates a file specified by the <code>initialization.completion.signal.file</code> parameter of servlet
 * context.</p>
 *
 * @author isv
 */
public class InitializationCompletionSignalingModule extends AbstractModule {

    /**
     * <p>A logger to be use for logging the details on database initialization.</p>
     */
    private static final Logger log = LoggerFactory.getLogger(InitializationCompletionSignalingModule.class);

    /**
     * <p>A current servlet context.</p>
     */
    private final ServletContext context;

    /**
     * <p>Constructs new <code>InitializationCompletionSignalingModule</code> instance. This implementation does nothing.</p>
     */
    public InitializationCompletionSignalingModule(ServletContext context) {
        this.context = context;
    }

    /**
     * <p>Signals on application initialization completion.</p>
     */
    @Override
    protected void configure() {
        final String signalFilePath = this.context.getInitParameter("initialization.completion.signal.file");
        if (signalFilePath != null && !signalFilePath.trim().isEmpty()) {
            File signalFile  = new File(signalFilePath);
            if (!signalFile.exists()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(signalFile))) {
                    pw.print(1);
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
}
