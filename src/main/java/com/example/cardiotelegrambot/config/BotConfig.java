package com.example.cardiotelegrambot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${telegram.bot.main-token}")
    private String mainToken;

    @Value("${telegram.bot.dev-token}")
    private String devToken;

    @Value("${telegram.bot.logger-token}")
    private String tokenLogger;

    @Value("${telegram.is-dev-mode}")
    private Boolean isDevMode;

    @Bean("mainBotBean")
    public TelegramBot newTelegramBot() {
        if (isDevMode) {
            return new TelegramBot(devToken);
        }
        return new TelegramBot(mainToken);
    }

    @Bean("loggerBotBean")
    public TelegramBot newTelegramBotLogger() {
        return new TelegramBot(tokenLogger);
    }
}
