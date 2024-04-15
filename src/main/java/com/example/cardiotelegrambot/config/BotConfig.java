package com.example.cardiotelegrambot.config;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

/**
 * This is a configuration class for the Telegram bot.
 * It uses the Lombok library for getter methods and Spring's @Configuration and @PropertySource annotations.
 * It provides a property for the bot token and channel ID, and a method to create a new Telegram bot.
 */
@PropertySource("classpath:hidden.properties")
@Configuration
public class BotConfig {

    /**
     * This is a property for the bot token.
     * It is injected from the hidden properties file using Spring's @Value annotation.
     */
    @Value("${telegram.bot.token}")
    private String token;

    /**
     * This is a property for the channel ID.
     * It is injected from the hidden properties file using Spring's @Value annotation.
     * It is also accessible through a getter method.
     */
    @Value("${telegram.channel.id}")
    @Getter
    private Long channelId;

    /**
     * This method creates a new Telegram bot.
     * It uses the bot token property to create the bot.
     * It is annotated with Spring's @Bean and @Scope annotations to ensure that only one instance of the bot is created.
     *
     * @return A new instance of TelegramBot.
     */
    @Bean
    @Scope("singleton")
    public TelegramBot newTelegramBot() {
        return new TelegramBot(token);
    }
}
