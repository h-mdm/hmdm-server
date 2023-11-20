package com.hmdm.guice.module;

import com.google.inject.servlet.ServletModule;
import com.hmdm.plugin.rest.PluginAccessFilter;
import com.hmdm.rest.filter.ApiOriginFilter;
import com.hmdm.rest.filter.PublicIPFilter;

/**
 * <p>A main module for REST API. Configures the common behavior for all resources.</p>
 *
 * @author isv
 */
public class MainRestModule extends ServletModule {

    /**
     * <p>Constructs new <code>MainRestModule</code> instance. This implementation does nothing.</p>
     */
    public MainRestModule() {
    }

    protected void configureServlets() {
        this.filter("/rest/*").through(ApiOriginFilter.class);
        // Deprecated and shouldn't be used
        this.filter("/api/*").through(ApiOriginFilter.class);
    }

}
