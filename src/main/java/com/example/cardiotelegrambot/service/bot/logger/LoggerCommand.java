package com.example.cardiotelegrambot.service.bot.logger;

import com.example.cardiotelegrambot.config.enums.logger.LoggerButtons;
import com.example.cardiotelegrambot.config.enums.logger.LoggerCommands;
import com.example.cardiotelegrambot.exceptions.NotAdminException;
import com.example.cardiotelegrambot.exceptions.NotCommandException;
import com.example.cardiotelegrambot.exceptions.NotMemberException;
import com.example.cardiotelegrambot.service.bot.main.Button;
import com.example.cardiotelegrambot.entity.UserEntity;
import com.example.cardiotelegrambot.repository.UserRepository;
import com.example.cardiotelegrambot.service.database.ReferralService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class LoggerCommand {

    private final TelegramBot loggerBot;
    private final TelegramBot mainBot;
    private final ReferralService referralService;
    private final Button button;
    private final UserRepository userRepository;

    @Value("${telegram.logger.id}")
    private Long adminChatId;

    @Value("${telegram.logger.dev-chat-id}")
    private Long devChatId;

    @Value("${telegram.is-dev-mode}")
    private Boolean isDevMode;

    private Long chatId;
    private final Map<LoggerCommands, Runnable> mapCommands;
    private Long userChatId;
    private String broadcastMessageText;

    @Autowired
    public LoggerCommand(@Qualifier("loggerBotBean") TelegramBot loggerBot, 
                         @Qualifier("mainBotBean") TelegramBot mainBot,
                         ReferralService referralService, 
                         Button button, 
                         UserRepository userRepository) {
        this.loggerBot = loggerBot;
        this.mainBot = mainBot;
        this.referralService = referralService;
        this.button = button;
        this.userRepository = userRepository;

        mapCommands = new HashMap<>();
        mapCommands.put(LoggerCommands.start, this::start);
        mapCommands.put(LoggerCommands.check, this::check);
        mapCommands.put(LoggerCommands.message, this::message);
    }

    @PostConstruct
    public void init() {
        chatId = isDevMode ? devChatId : adminChatId;
    }

    public Runnable getCommand(String command) {
        try {
            Pair<LoggerCommands, Long> pair = LoggerCommands.fromString(command);
            userChatId = pair.getSecond();
            if (pair.getFirst() == LoggerCommands.message) {
                int firstSpaceIndex = command.indexOf(' ');
                broadcastMessageText = firstSpaceIndex > -1 && firstSpaceIndex + 1 < command.length()
                        ? command.substring(firstSpaceIndex + 1)
                        : "";
            }
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

        if (!chatId.equals(this.chatId)) {
            throw new NotAdminException();
        }

        return this;
    }

    public void start() {
        SendMessage message = new SendMessage(
                chatId,
                String.format("""
                        Привет, братанчик! Тебе пишет твой верный слуга, бот для админов (в этом канале я заправляю, как ты уже понял). Выбери нужную кнопку внизу! Также ты можешь ввести команду /check <chat id юзера>, и ты узнаешь подписан ли этот человек на твой канал.
                        
                        Сейчас реферальная программа %s.
                        
                        Также доступна рассылка: /message <текст> — отправит сообщение всем пользователям бота.

                        *Не бойся csv файла, просто открой его, он должен открыться как таблица через приложение Numbers на MacOS или Excel на Windows.
                        """,
                        referralService.isPresent() ? "АКТИВНА": "НЕ АКТИВНА"
                ));
        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());

        SendResponse response = loggerBot.execute(message);
        LoggerButton.setMessageId(response.message().messageId());
    }

    private void check() {
        SendMessage message;
        try {
            button
                    .setByVariables(userChatId)
                    .isSubscribed();
            message = new SendMessage(
                    chatId,
                    String.format("""
                            Пользователь %s подписан на твой канал.
                            """, userChatId
                    )
            );
        } catch (NotMemberException | NullPointerException exception) {
            message = new SendMessage(
                    chatId,
                    String.format("""
                            Пользователь %s НЕ подписан на твой канал.
                            """, userChatId
                    )
            );
        }
        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());

        SendResponse response = loggerBot.execute(message);
        LoggerButton.setMessageId(response.message().messageId());
    }

    private void message() {
        if (broadcastMessageText == null || broadcastMessageText.isBlank()) {
            SendMessage warn = new SendMessage(
                    chatId,
                    "Текст сообщения пуст. Используй: /message <текст>"
            );
            warn.replyMarkup(getInlineKeyboardMarkupForMainMenu());
            loggerBot.execute(warn);
            return;
        }

        int success = 0;
        int failed = 0;

        Set<Long> sentChatIds = new HashSet<>();
        List<UserEntity> users = userRepository.findAll();

        for (UserEntity user : users) {
            Long mainChatId = user.getChatId();
            if (mainChatId != null && sentChatIds.add(mainChatId)) {
                try {
                    mainBot.execute(new SendMessage(mainChatId, broadcastMessageText));
                    success++;
                } catch (Exception ignored) {
                    failed++;
                }
            }
        }

        SendMessage done = new SendMessage(
                chatId,
                String.format("Рассылка завершена. Успешно: %d, Ошибок: %d.", success, failed)
        );
        done.replyMarkup(getInlineKeyboardMarkupForMainMenu());
        SendResponse response = loggerBot.execute(done);
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
                chatId,
                "Братан, ты какую-то херню написал. Лучше просто нажми одну из доступных кнопок - не ошибешься. Либо напиши команду /start."
        );
        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());
        loggerBot.execute(message);
    }
}
