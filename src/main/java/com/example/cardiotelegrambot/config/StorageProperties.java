package com.example.cardiotelegrambot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is a configuration class for storage properties.
 * It uses the Lombok library for getter and setter methods and Spring's @Configuration and @ConfigurationProperties annotations.
 * It provides a property for the location of the storage.
 */
@ConfigurationProperties("storage")
@Configuration
@Getter
@Setter
public class StorageProperties {

    /**
     * This is a property for the location of the storage.
     * It is injected from the application properties file using Spring's @Value annotation.
     */
    @Value("${spring.file.location}")
    private String location;
}
