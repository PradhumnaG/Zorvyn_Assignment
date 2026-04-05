package com.finance.zorvyn.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()

                .info(new Info()
                        .title("Finance Dashboard API")
                        .description("Backend API for the Finance Dashboard system. " +
                                "Supports user management, financial records, " +
                                "dashboard analytics, and role-based access control.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Finance Dashboard Team")
                                .email("dev@financedashboard.com")))


                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token (without 'Bearer ' prefix)")))


                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
