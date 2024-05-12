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

    private enum LogType {
        ERROR,
        WARN,
        INFO
    }

    @Value("${telegram.bot.admin}")
    private Long chatId;

    private final TelegramBot bot;
    private final TelegramBot urgentBot;

    @Autowired
    public LogConfig(@Qualifier("loggerBotBean") TelegramBot bot, @Qualifier("urgentBotBean") TelegramBot urgentBot) {
        this.bot = bot;
        this.urgentBot = urgentBot;
    }

    private void sendMessage(LogType logType, String message) {
        String finalMessage = String.format("%s %s : %s", LocalDateTime.now(), logType, message);

        if (logType == LogType.ERROR) {
            urgentBot.execute(new SendMessage(
                    chatId,
                    finalMessage
            ));
        } else {
            bot.execute(new SendMessage(
                    chatId,
                    finalMessage
            ));
        }
    }

    public void error(String message) {
        sendMessage(LogType.ERROR, message);
    }

    public void warn(String message) {
        sendMessage(LogType.WARN, message);
    }

    public void info(String message) {
        sendMessage(LogType.INFO, message);
    }
}
