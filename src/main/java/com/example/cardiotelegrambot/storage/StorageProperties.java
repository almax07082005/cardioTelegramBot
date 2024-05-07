package com.example.cardiotelegrambot.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("storage")
@Configuration
@Getter
@Setter
public class StorageProperties {

    @Value("${spring.file.location}")
    private String location;
}
