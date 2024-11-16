package com.example.cardiotelegrambot.service.bot.logger;

import com.example.cardiotelegrambot.config.enums.logger.LoggerButtons;
import com.example.cardiotelegrambot.config.enums.logger.LoggerCommands;
import com.example.cardiotelegrambot.exceptions.NotAdminException;
import com.example.cardiotelegrambot.exceptions.NotCommandException;
import com.example.cardiotelegrambot.exceptions.NotMemberException;
import com.example.cardiotelegrambot.service.bot.main.Button;
import com.example.cardiotelegrambot.service.database.ReferralService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoggerCommand {

    private final TelegramBot bot;
    private final ReferralService referralService;
    private final Button button;

    @Value("${telegram.logger.id}")
    private Long adminChatId;

    private final Map<LoggerCommands, Runnable> mapCommands;
    private Long userChatId;

    @Autowired
    public LoggerCommand(@Qualifier("loggerBotBean") TelegramBot bot, ReferralService referralService, Button button) {
        this.bot = bot;
        this.referralService = referralService;
        this.button = button;

        mapCommands = new HashMap<>();
        mapCommands.put(LoggerCommands.start, this::start);
        mapCommands.put(LoggerCommands.check, this::check);
    }

    public Runnable getCommand(String command) {
        try {
            Pair<LoggerCommands, Long> pair = LoggerCommands.fromString(command);
            userChatId = pair.getSecond();
            return mapCommands.get(pair.getFirst());
        } catch (NotCommandException exception) {
            return this::notACommand;
        }
    }

    public LoggerCommand setByUpdate(Update update) throws NotAdminException {
        Long chatId = update
                .message()
                .chat()
                .id();

        if (!chatId.equals(adminChatId)) {
            throw new NotAdminException();
        }

        return this;
    }

    public void start() {
        SendMessage message = new SendMessage(
                adminChatId,
                String.format("""
                        Привет, братанчик! Тебе пишет твой верный слуга, бот для админов (в этом канале я заправляю, как ты уже понял). Выбери нужную кнопку внизу! Также ты можешь ввести команду /check <chat id юзера>, и ты узнаешь подписан ли этот человек на твой канал.
                        
                        Сейчас реферальная программа %s.
                        
                        *Не бойся csv файла, просто открой его, он должен открыться как таблица через приложение Numbers на MacOS или Excel на Windows.
                        """,
                        referralService.isPresent() ? "АКТИВНА": "НЕ АКТИВНА"
                ));
        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());

        SendResponse response = bot.execute(message);
        LoggerButton.setMessageId(response.message().messageId());
    }

    private void check() {
        SendMessage message;
        try {
            button
                    .setByVariables(userChatId)
                    .isSubscribed();
            message = new SendMessage(
                    adminChatId,
                    String.format("""
                            Пользователь %s подписан на твой канал.
                            """, userChatId
                    )
            );
        } catch (NotMemberException | NullPointerException exception) {
            message = new SendMessage(
                    adminChatId,
                    String.format("""
                            Пользователь %s НЕ подписан на твой канал.
                            """, userChatId
                    )
            );
        }
        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());

        SendResponse response = bot.execute(message);
        LoggerButton.setMessageId(response.message().messageId());
    }

    public InlineKeyboardMarkup getInlineKeyboardMarkupForMainMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Получить главное сообщение").callbackData(LoggerButtons.start.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Начать рефералку").callbackData(LoggerButtons.startReferral.name()),
                new InlineKeyboardButton("Завершить рефералку").callbackData(LoggerButtons.finishReferral.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Получить таблицу победителей").callbackData(LoggerButtons.getWinners.name())
        );

        return inlineKeyboardMarkup;
    }

    private void notACommand() {
        SendMessage message = new SendMessage(
                adminChatId,
                "Братан, ты какую-то херню написал. Лучше просто нажми одну из доступных кнопок - не ошибешься. Либо напиши команду 'start'."
        );
        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());
        bot.execute(message);
    }
}
