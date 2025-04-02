package com.example.cardiotelegrambot.service.bot.main;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.config.enums.main.Buttons;
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
    public BotService(@Qualifier("mainBotBean") TelegramBot bot,
                      Button button,
                      Command command,
                      Logger logger) {
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
                    else if (update.message() != null) executeCommand(update);
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

    private void executeButton(Update update) {
        logger.logInfo(String.format(
                "\"%s\"_%s pressed button: \"%s\".",
                update.callbackQuery().from().username(),
                update.callbackQuery().from().id(),
                update.callbackQuery().data()
        ));
        try {
            button
                    .setByUpdate(update)
                    .getButton(Buttons.valueOf(update
                            .callbackQuery()
                            .data()
                    ))
                    .run();
        } catch (IllegalArgumentException exception) {
            logger.logWarn(String.format(
                    "Button \"%s\" isn't found.",
                    update.callbackQuery().data()
            ));
        }
    }

    private void executeCommand(Update update) {
        logger.logInfo(String.format(
                "\"%s\"_%s sent message: \"%s\".",
                update.message().from().username(),
                update.message().chat().id(),
                update.message().text()
        ));
        command
                .setByUpdate(update)
                .getCommand(update
                        .message()
                        .text())
                .run();
    }
}
