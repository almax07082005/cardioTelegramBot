package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.LogConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This is a service class for the bot.
 * It uses the TelegramBot library for interacting with the Telegram API and the Spring framework for dependency injection and configuration.
 * It provides methods for starting the bot and executing commands.
 */
@Component
public class Bot {

    private final TelegramBot bot;
    private final ConfigurableApplicationContext context;

    /**
     * This is a constructor that takes a TelegramBot object and a ConfigurableApplicationContext object as input.
     * It uses these objects to set the bot and context properties.
     *
     * @param bot A TelegramBot object for interacting with the Telegram API.
     * @param context A ConfigurableApplicationContext object for accessing application context.
     */
    @Autowired
    public Bot(TelegramBot bot, ConfigurableApplicationContext context) {
        this.bot = bot;
        this.context = context;
    }

    /**
     * This method starts the bot.
     * It sets an updates listener on the bot that executes commands for each update and logs any exceptions.
     */
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

    /**
     * This method executes a command.
     * It takes an Update object as input and tries to execute the command in the update.
     * If the command is not recognized, it calls the notACommand method.
     *
     * @param update An Update object representing an incoming update from Telegram.
     */
    private void executeCommand(Update update) {
        Command command = context.getBean(Command.class).setByUpdate(update);

        try {
            command.getMapCommands().get(update.message().text()).run();
        } catch (NullPointerException ignored) {
            command.notACommand();
        }
    }
}
