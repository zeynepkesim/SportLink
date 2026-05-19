package com.sportlink.activitymanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customises the metadata shown at /swagger-ui.html.
 * Endpoints themselves are auto-discovered by springdoc.
 *
 * Defines a "bearerAuth" security scheme so the Swagger UI shows an
 * "Authorize" button and lets users paste their JWT for trying out
 * protected endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI activityManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SportLink - Activity Management Service")
                        .description("Manages the lifecycle of sports activities: creation, "
                                + "updates, cancellation, join/leave, and participant management.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste a JWT obtained from POST /api/auth/login on Account Management")));
    }
}