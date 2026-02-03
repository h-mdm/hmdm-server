package com.hmdm;

import com.google.inject.Injector;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * A configuration for HMDM server application.
 * </p>
 *
 * <p>
 * Updated for OpenAPI 3.x (Swagger 2.x) compatibility.
 * </p>
 *
 * @author isv
 */
public class HMDMApplication extends ResourceConfig {

    /**
     * <p>
     * Constructs new <code>HMDMApplication</code> instance and initializes the
     * Guice-HK2 bridge.
     * </p>
     */
    @Inject
    public HMDMApplication(final ServiceLocator serviceLocator) {
        packages("com.hmdm");
        register(MultiPartFeature.class);
        register(new ContainerLifecycleListener() {
            public void onStartup(Container container) {
                ServletContainer servletContainer = (ServletContainer) container;
                GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
                GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
                Injector injector = (Injector) servletContainer.getServletContext()
                        .getAttribute(Injector.class.getName());
                guiceBridge.bridgeGuiceInjector(injector);

                // OpenAPI 3.x configuration (replaces Swagger 1.x BeanConfig)
                OpenAPI openAPI = new OpenAPI();
                openAPI.info(new Info()
                        .title("Headwind MDM API")
                        .version("0.0.2"));

                SwaggerConfiguration swaggerConfig = new SwaggerConfiguration()
                        .openAPI(openAPI)
                        .prettyPrint(true)
                        .resourcePackages(Stream.of("com.hmdm").collect(Collectors.toSet()));
            }

            public void onReload(Container container) {
            }

            public void onShutdown(Container container) {
            }
        });

        // OpenAPI 3.x resource (replaces Swagger 1.x ApiListingResource and
        // SwaggerSerializers)
        register(OpenApiResource.class);

    }

}
