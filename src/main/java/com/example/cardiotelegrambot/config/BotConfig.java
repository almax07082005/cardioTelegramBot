package com.example.cardiotelegrambot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${telegram.bot.main-token}")
    private String token;

    @Value("${telegram.bot.logger-token}")
    private String tokenLogger;

    @Bean("mainBotBean")
    public TelegramBot newTelegramBot() {
        return new TelegramBot(token);
    }

    @Bean("loggerBotBean")
    public TelegramBot newTelegramBotLogger() {
        return new TelegramBot(tokenLogger);
    }
}
