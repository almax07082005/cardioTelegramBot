package com.example.cardiotelegrambot.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.LocalDateTime;

@Configuration
@PropertySource("classpath:hidden.properties")
public class LogConfig {

    @Value("${telegram.bot.admin}")
    private Long chatId;

    private final TelegramBot bot;

    @Autowired
    public LogConfig(@Qualifier("loggerBotBean") TelegramBot bot) {
        this.bot = bot;
    }

    private void sendMessage(String message) {
        bot.execute(new SendMessage(
                chatId,
                message
        ));
    }

    public void error(String message) {
        sendMessage(LocalDateTime.now() + " ERROR : " + message);
    }

    public void warn(String message) {
        sendMessage(LocalDateTime.now() + " WARN : " + message);
    }

    public void info(String message) {
        sendMessage(LocalDateTime.now() + " INFO : " + message);
    }
}
