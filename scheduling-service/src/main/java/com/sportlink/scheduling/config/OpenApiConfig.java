package com.sportlink.scheduling.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schedulingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SportLink — Scheduling Service")
                        .description("Resolves user and facility availability into "
                                + "concrete bookable time slots. Used by Activity "
                                + "Management, Booking, and Matching services.")
                        .version("v1")
                        )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste a JWT obtained from POST /api/auth/login on Account Management")));
    }
}