package com.example.orgo_project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orgoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ORGO Payout API")
                        .description("Swagger/OpenAPI cho module Payout / Wallet / Transaction")
                        .version("1.0.0")
                        .contact(new Contact().name("ORGO Team").email("support@orgo.local")));
    }
}
