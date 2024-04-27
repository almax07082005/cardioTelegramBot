package com.example.cardiotelegrambot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:hidden.properties")
@Configuration
public class BotConfig {

    @Value("${telegram.bot.token}")
    private String token;

    @Bean
    public TelegramBot newTelegramBot() {
        return new TelegramBot(token);
    }
}
