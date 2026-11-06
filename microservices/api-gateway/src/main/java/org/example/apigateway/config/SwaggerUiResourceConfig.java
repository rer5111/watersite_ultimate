package org.example.apigateway.config;

import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.webflux.ui.SwaggerIndexTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.resource.ResourceWebHandler;

import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerUiResourceConfig {

    @Bean
    SimpleUrlHandlerMapping swaggerUiResourceMapping(
            SwaggerIndexTransformer indexTransformer,
            SpringDocConfigProperties springDocConfigProperties,
            SwaggerUiConfigProperties swaggerUiConfigProperties
    ) throws Exception {
        String version = swaggerUiConfigProperties.getVersion();
        if (version == null || version.isBlank()) {
            version = "5.21.0";
        }

        ResourceWebHandler swaggerUiHandler = new ResourceWebHandler();
        swaggerUiHandler.setLocations(List.of(
                new ClassPathResource("META-INF/resources/webjars/swagger-ui/" + version + "/"),
                new ClassPathResource("META-INF/resources/webjars/")
        ));
        swaggerUiHandler.setResourceTransformers(List.of(indexTransformer));
        swaggerUiHandler.afterPropertiesSet();

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(0);
        mapping.setUrlMap(Map.of(
                springDocConfigProperties.getWebjars().getPrefix() + "/**", swaggerUiHandler,
                "/swagger-ui/**", swaggerUiHandler
        ));
        return mapping;
    }
}
