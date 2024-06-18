package com.example.cardiotelegrambot.service.bot.logger;

import com.example.cardiotelegrambot.config.enums.logger.LoggerButtons;
import com.example.cardiotelegrambot.service.database.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoggerButton {

    private final TelegramBot bot;
    private final Map<LoggerButtons, Runnable> buttons;
    private final LoggerCommand loggerCommand;
    private final UserService userService;

    @Setter
    private static Integer messageId;

    @Value("${spring.data.table}")
    private String tableFilename;

    @Value("${spring.data.referral}")
    private String referralFilename;

    @Value("${telegram.logger.id}")
    private Long adminChatId;

    @Autowired
    public LoggerButton(@Qualifier("loggerBotBean") TelegramBot bot, LoggerCommand loggerCommand, UserService userService) {
        this.bot = bot;

        buttons = new HashMap<>();
        buttons.put(LoggerButtons.start, loggerCommand::start);
        buttons.put(LoggerButtons.startReferral, this::startReferral);
        buttons.put(LoggerButtons.finishReferral, this::finishReferral);
        buttons.put(LoggerButtons.getWinners, this::getWinners);
        this.loggerCommand = loggerCommand;
        this.userService = userService;
    }

    public LoggerButton deleteLastMessage() {
        try {
            bot.execute(new DeleteMessage(
                    adminChatId,
                    messageId
            ));
        } catch (NullPointerException ignored) {}

        return this;
    }

    public Runnable getButton(LoggerButtons button) {
        return buttons.get(button);
    }

    private void getWinners() {
        userService.storeUsersToCSV();
        SendDocument document = new SendDocument(
                adminChatId,
                new File(tableFilename)
        );
        document.replyMarkup(loggerCommand.getInlineKeyboardMarkupForMainMenu());

        SendResponse response = bot.execute(document);
        messageId = response.message().messageId();
    }

    private void startReferral() {
        SendMessage message;

        try (FileWriter fileWriter = new FileWriter(referralFilename)) {
            fileWriter.write("true");
            message = new SendMessage(
                    adminChatId,
                    "Реферальная программа запущена успешно."
            );
        } catch (IOException ignored) {
            message = new SendMessage(
                    adminChatId,
                    "Фиг его знает, че-то не сработало, надо разбираться."
            );
        }

        message.replyMarkup(loggerCommand.getInlineKeyboardMarkupForMainMenu());

        SendResponse response = bot.execute(message);
        messageId = response.message().messageId();
    }

    private void finishReferral() {
        SendMessage message;

        try (FileWriter fileWriter = new FileWriter(referralFilename)) {
            fileWriter.write("false");
            message = new SendMessage(
                    adminChatId,
                    "Реферальная программа завершена успешно."
            );
        } catch (IOException ignored) {
            message = new SendMessage(
                    adminChatId,
                    "Фиг его знает, че-то не сработало, надо разбираться."
            );
        }

        message.replyMarkup(loggerCommand.getInlineKeyboardMarkupForMainMenu());

        SendResponse response = bot.execute(message);
        messageId = response.message().messageId();
    }
}
