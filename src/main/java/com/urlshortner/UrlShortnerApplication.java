package com.urlshortner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the URL Shortener Spring Boot application.
 *
 * @SpringBootApplication combines:
 *   - @Configuration       → marks this as a config class
 *   - @EnableAutoConfiguration → auto-configures Spring based on classpath
 *   - @ComponentScan       → scans this package and sub-packages for beans
 */
@SpringBootApplication
public class UrlShortnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortnerApplication.class, args);
    }
}
