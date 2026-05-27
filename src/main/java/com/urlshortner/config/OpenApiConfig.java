package com.urlshortner.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CONFIGURATION CLASS
 *
 * @Configuration → tells Spring: "this class contains bean definitions".
 *   Beans defined here are created once and shared across the whole application.
 *
 * @Bean → the return value of this method is registered as a Spring bean.
 *   Spring manages its lifecycle (creation, injection, destruction).
 *
 * This config customizes the Swagger UI title, description, and version
 * visible at http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .version("1.0.0")
                        .description("A Spring Boot REST API to shorten URLs, track clicks, and manage short links.")
                        .contact(new Contact()
                                .name("Sarthak")
                                .url("https://github.com/sarthakshahane/url-shortner")));
    }
}
