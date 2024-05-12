package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.config.enums.Buttons;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BotService {

    private final TelegramBot bot;
    private final Button button;
    private final Command command;
    private final Logger logger;

    @Autowired
    public BotService(@Qualifier("mainBotBean") TelegramBot bot, Button button, Command command, Logger logger) {
        this.bot = bot;
        this.button = button;
        this.command = command;
        this.logger = logger;
    }

    public void startBot() {
        bot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    if (update.callbackQuery() != null) executeButton(update);
                    else executeCommand(update);
                }
            } catch (Exception exception) {
                logger.logError(exception);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, exception -> {
            if (exception.response() != null) {
                exception.response().errorCode();
                exception.response().description();
            } else {
                logger.logError(exception);
            }
        });
    }

    private void executeButton(Update update) {
        logger.logInfo(
                "@" +
                update.callbackQuery().from().username() +
                " pressed button: \"" +
                update.callbackQuery().data() +
                "\""
        );

        button
                .setByUpdate(update)
                .getButton(Buttons.valueOf(update
                        .callbackQuery()
                        .data()))
                .run();
    }

    private void executeCommand(Update update) {
        logger.logInfo(
                "@" +
                update.message().from().username() +
                " sent message: \"" +
                update.message().text() +
                "\""
        );

        command
                .setByUpdate(update)
                .getCommand(update
                        .message()
                        .text())
                .run();
    }
}
