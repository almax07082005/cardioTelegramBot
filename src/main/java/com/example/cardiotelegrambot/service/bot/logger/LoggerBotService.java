package com.example.cardiotelegrambot.service.bot.logger;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.exceptions.NotAdminException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class LoggerBotService {

    private final TelegramBot bot;
    private final Logger logger;
    private final LoggerCommand command;

    @Autowired
    public LoggerBotService(@Qualifier("loggerBotBean") TelegramBot bot, Logger logger, LoggerCommand command) {
        this.bot = bot;
        this.logger = logger;
        this.command = command;
    }

    public void startBot() {
        bot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    executeCommand(update);
                }
            } catch (Exception exception) {
                logger.logException(exception);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, exception -> {
            if (exception.response() != null) {
                exception.response().errorCode();
                exception.response().description();
            } else {
                logger.logException(exception);
            }
        });
    }

    private void executeCommand(Update update) {
        try {
            command
                    .setByUpdate(update)
                    .getCommand(update
                            .message()
                            .text())
                    .run();
        } catch (NotAdminException exception) {
            bot.execute(new SendMessage(
                    update.message().chat().id(),
                    exception.getMessage()
            ));
        }
    }
}
