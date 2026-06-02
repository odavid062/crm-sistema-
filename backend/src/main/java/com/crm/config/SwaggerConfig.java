package com.crm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CRM Sistema API")
                        .version("1.0.0")
                        .description("API completa do CRM com integrações UazAPI (WhatsApp), Asaas (Pagamentos) e n8n (Automação)")
                        .contact(new Contact()
                                .name("CRM Sistema")
                                .email("contato@crm-sistema.com"))
                        .license(new License().name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desenvolvimento"),
                        new Server().url("https://api.crm-sistema.com").description("Produção")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido via POST /api/auth/login")));
    }
}
