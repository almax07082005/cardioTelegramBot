package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.LogConfig;
import com.example.cardiotelegrambot.config.enums.Buttons;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class BotService {

    private final TelegramBot bot;
    private final ConfigurableApplicationContext context;

    @Autowired
    public BotService(TelegramBot bot, ConfigurableApplicationContext context) {
        this.bot = bot;
        this.context = context;
    }

    public void startBot() {
        bot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    if (update.callbackQuery() != null) executeButton(update);
                    else executeCommand(update);
                }
            } catch (Exception exception) {
                LogConfig.logError(exception);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, exception -> {
            if (exception.response() != null) {
                exception.response().errorCode();
                exception.response().description();
            } else {
                LogConfig.logError(exception);
            }
        });
    }

    private void executeButton(Update update) {
        LogConfig.logInfo(
                "@" +
                update.callbackQuery().from().username() +
                " pressed button: \"" +
                update.callbackQuery().data() +
                "\""
        );

        context.getBean(Button.class)
                .setByUpdate(update)
                .getButton(Buttons.valueOf(update
                        .callbackQuery()
                        .data()))
                .run();
    }

    private void executeCommand(Update update) {
        LogConfig.logInfo(
                "@" +
                update.message().from().username() +
                " sent message: \"" +
                update.message().text() +
                "\""
        );

        context.getBean(Command.class)
                .setByUpdate(update)
                .getCommand(update
                        .message()
                        .text())
                .run();
    }
}
