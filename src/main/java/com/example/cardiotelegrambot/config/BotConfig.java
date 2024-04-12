package com.example.cardiotelegrambot.config;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

@PropertySource("classpath:hidden.properties")
@Configuration
public class BotConfig {

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.channel.id}")
    @Getter
    private Long channelId;

    @Bean
    @Scope("singleton")
    public TelegramBot newTelegramBot() {
        return new TelegramBot(token);
    }
}
