package com.example.cardiotelegrambot.service.bot.logger;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.config.enums.logger.LoggerButtons;
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
    private final LoggerButton button;

    @Autowired
    public LoggerBotService(@Qualifier("loggerBotBean") TelegramBot bot,
                            Logger logger,
                            LoggerCommand command,
                            LoggerButton button) {
        this.bot = bot;
        this.logger = logger;
        this.command = command;
        this.button = button;
    }

    public void startBot() {
        bot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    if (update.callbackQuery() != null) executeButton(update);
                    else executeCommand(update);
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
        Long chatId = update
                .callbackQuery()
                .message()
                .chat()
                .id();

        try {
            button
                    .isAdmin(chatId)
                    .deleteLastMessage()
                    .getButton(LoggerButtons.valueOf(update
                            .callbackQuery()
                            .data()
                    ))
                    .run();
        } catch (NotAdminException exception) {
            SendMessage message = new SendMessage(
                    chatId,
                    exception.getMessage()
            );
            bot.execute(message);
        }
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
            SendMessage message = new SendMessage(
                    update.message().chat().id(),
                    exception.getMessage()
            );
            bot.execute(message);
        }
    }
}
