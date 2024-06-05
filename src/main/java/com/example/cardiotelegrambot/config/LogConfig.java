package com.example.cardiotelegrambot.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Configuration
public class LogConfig {

    private enum LogType {
        ERROR,
        WARN,
        INFO
    }

    @Value("${telegram.logger.id}")
    private Long chatId;

    @Value("${telegram.logger.info}")
    private Integer infoId;

    @Value("${telegram.logger.warn}")
    private Integer warnId;

    @Value("${telegram.logger.error}")
    private Integer errorId;

    private final TelegramBot loggerBot;

    @Autowired
    public LogConfig(@Qualifier("loggerBotBean") TelegramBot loggerBot) {
        this.loggerBot = loggerBot;
    }

    private void sendMessage(LogType logType, String message, Integer messageThreadId) {
        String finalMessage = String.format(
                "%s %s : %s",
                ZonedDateTime.now(ZoneId.of("Etc/GMT+9")).toLocalDateTime(),
                logType,
                message
        );

        loggerBot.execute(new SendMessage(
                chatId,
                finalMessage
        ).messageThreadId(messageThreadId));
    }

    public void error(String message) {
        sendMessage(LogType.ERROR, message, errorId);
    }

    public void warn(String message) {
        sendMessage(LogType.WARN, message, warnId);
    }

    public void info(String message) {
        sendMessage(LogType.INFO, message, infoId);
    }
}
