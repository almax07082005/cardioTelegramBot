package com.example.cardiotelegrambot.service.bot.logger;

import com.example.cardiotelegrambot.config.enums.logger.LoggerButtons;
import com.example.cardiotelegrambot.exceptions.NotAdminException;
import com.example.cardiotelegrambot.service.database.ReferralService;
import com.example.cardiotelegrambot.service.database.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
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
    private final ReferralService referralService;

    @Value("${spring.data.table}")
    private String tableFilename;

    @Value("${telegram.logger.id}")
    private Long adminChatId;
    
    @Value("${telegram.logger.dev-chat-id}")
    private Long devChatId;

    @Value("${telegram.is-dev-mode}")
    private Boolean isDevMode;

    private Long chatId;

    @Autowired
    public LoggerButton(@Qualifier("loggerBotBean") TelegramBot bot, LoggerCommand loggerCommand, UserService userService, ReferralService referralService) {
        this.bot = bot;

        buttons = new HashMap<>();
        buttons.put(LoggerButtons.start, loggerCommand::start);
        buttons.put(LoggerButtons.startReferral, this::startReferral);
        buttons.put(LoggerButtons.finishReferral, this::finishReferral);
        buttons.put(LoggerButtons.getWinners, this::getWinners);
        this.loggerCommand = loggerCommand;
        this.userService = userService;
        this.referralService = referralService;
    }

    @PostConstruct
    public void init() {
        chatId = isDevMode ? devChatId : adminChatId;
    }

    public LoggerButton isAdmin(Long chatId) throws NotAdminException {
        if (!chatId.equals(this.chatId)) {
            throw new NotAdminException();
        }

        return this;
    }

    public LoggerButton deleteLastMessage() {
        try {
            bot.execute(new DeleteMessage(
                    chatId,
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
                chatId,
                new File(tableFilename)
        );
        document.replyMarkup(loggerCommand.getInlineKeyboardMarkupForMainMenu());

        SendResponse response = bot.execute(document);
        messageId = response.message().messageId();
    }

    private void startReferral() {
        referralService.startReferral();
        SendMessage message = new SendMessage(
                chatId,
                "Реферальная программа запущена успешно."
        );
        message.replyMarkup(loggerCommand.getInlineKeyboardMarkupForMainMenu());

        SendResponse response = bot.execute(message);
        messageId = response.message().messageId();
    }

    private void finishReferral() {
        referralService.finishReferral();
        SendMessage message = new SendMessage(
                chatId,
                "Реферальная программа завершена успешно."
        );
        message.replyMarkup(loggerCommand.getInlineKeyboardMarkupForMainMenu());

        SendResponse response = bot.execute(message);
        messageId = response.message().messageId();
    }
}
