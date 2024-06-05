package com.example.cardiotelegrambot.service.bot.logger;

import com.example.cardiotelegrambot.config.enums.LoggerCommands;
import com.example.cardiotelegrambot.exceptions.NotAdminException;
import com.example.cardiotelegrambot.exceptions.NotCommandException;
import com.example.cardiotelegrambot.service.bot.main.Command;
import com.example.cardiotelegrambot.service.database.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
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
public class LoggerCommand {

    private final TelegramBot bot;
    private final UserService userService;
    private final Command command;
    private Long chatId;

    @Value("${telegram.logger.id}")
    private Long adminChatId;

    @Value("${spring.data.table}")
    private String tableFilename;

    @Value("${spring.data.referral}")
    private String referralFilename;

    private final Map<LoggerCommands, Runnable> mapCommands;

    @Autowired
    public LoggerCommand(@Qualifier("loggerBotBean") TelegramBot bot, UserService userService, Command command) {
        this.bot = bot;
        this.userService = userService;
        this.command = command;

        mapCommands = new HashMap<>();
        mapCommands.put(LoggerCommands.start, this::start);
        mapCommands.put(LoggerCommands.startReferral, this::startReferral);
        mapCommands.put(LoggerCommands.finishReferral, this::finishReferral);
        mapCommands.put(LoggerCommands.getWinners, this::getWinners);
    }

    public Runnable getCommand(String command) {
        try {
            return mapCommands.get(LoggerCommands.fromString(command));
        } catch (NotCommandException exception) {
            return this::notACommand;
        }
    }

    public LoggerCommand setByUpdate(Update update) throws NotAdminException {
        chatId = update
                .message()
                .chat()
                .id();

        if (!chatId.equals(adminChatId)) {
            throw new NotAdminException();
        }

        return this;
    }

    private void finishReferral() {
        try (FileWriter fileWriter = new FileWriter(referralFilename)) {
            fileWriter.write("false");
            bot.execute(new SendMessage(
                    chatId,
                    "Реферальная программа остановлена успешно."
            ));
        } catch (IOException ignored) {
            bot.execute(new SendMessage(
                    chatId,
                    "Фиг его знает, че-то не сработало, надо разбираться."
            ));
        }
    }

    private void startReferral() {
        try (FileWriter fileWriter = new FileWriter(referralFilename)) {
            fileWriter.write("true");
            bot.execute(new SendMessage(
                    chatId,
                    "Реферальная программа запущена успешно."
            ));
        } catch (IOException ignored) {
            bot.execute(new SendMessage(
                    chatId,
                    "Фиг его знает, че-то не сработало, надо разбираться."
            ));
        }
    }

    private void start() {
        bot.execute(new SendMessage(
                chatId,
                String.format("""
                        Привет, братанчик Азамат (а может и не только ты)! Тебе пишет твой верный слуга, бот для админов (в этом канале я заправляю, как ты уже понял), и вот, что ты можешь у меня спросить (просто нажми на нужную команду):
                        
                        /start - получить это сообщение;
                        /startReferral - начать реферальную программу;
                        /finishReferral - завершить реферальную программу;
                        /getWinners - получить список победителей*.
                        
                        Сейчас реферальная программа %s.
                        
                        *Не бойся csv файла, просто открой его, он должен открыться как таблица через приложение Numbers на MacOS или Excel на Windows.
                        """,
                        command.getBooleanReferralStatus() ? "АКТИВНА": "НЕ АКТИВНА"
                )
        ));
    }

    private void getWinners() {
        userService.storeUsersToCSV();
        bot.execute(new SendDocument(
                chatId,
                new File(tableFilename)
        ));
    }

    private void notACommand() {
        bot.execute(new SendMessage(
                chatId,
                "Братан, ты какую-то херню написал. Выбери команду из списка. Список можно найти, нажав (или написав) '/start.'"
        ));
    }
}
