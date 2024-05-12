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

    @Value("${telegram.logger-bot.token}")
    private String tokenLogger;

    @Value("${telegram.urgent-bot.token}")
    private String tokenUrgent;

    @Bean("mainBotBean")
    public TelegramBot newTelegramBot() {
        return new TelegramBot(token);
    }

    @Bean("loggerBotBean")
    public TelegramBot newTelegramBotLogger() {
        return new TelegramBot(tokenLogger);
    }

    @Bean("urgentBotBean")
    public TelegramBot newTelegramBotUrgent() {
        return new TelegramBot(tokenUrgent);
    }
}
