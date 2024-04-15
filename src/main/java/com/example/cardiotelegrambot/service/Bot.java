package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.LogConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Bot {

    private final TelegramBot bot;
    private final ConfigurableApplicationContext context;

    @Autowired
    public Bot(TelegramBot bot, ConfigurableApplicationContext context) {
        this.bot = bot;
        this.context = context;
    }

    public void startBot() {
		bot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    executeCommand(update);
                }
            } catch (Exception exception) {
                LogConfig.logError(exception.getStackTrace());
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, exception -> {
            if (exception.response() != null) {
                exception.response().errorCode();
                exception.response().description();
            } else {
                LogConfig.logError(exception.getStackTrace());
            }
        });
    }

    private void executeCommand(Update update) {
        Command command = context.getBean(Command.class).setByUpdate(update);

        try {
            command.getMapCommands().get(update.message().text()).run();
        } catch (NullPointerException ignored) {
            command.notACommand();
        }
    }
}
