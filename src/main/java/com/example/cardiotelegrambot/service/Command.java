package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.LogConfig;
import com.example.cardiotelegrambot.config.enums.Buttons;
import com.example.cardiotelegrambot.config.enums.Commands;
import com.example.cardiotelegrambot.exceptions.NotCommandException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Command {

    private final TelegramBot bot;

    private Long id;
    private String firstName;
    private final Map<Commands, Runnable> mapCommands;

    @Autowired
    public Command(TelegramBot bot) {
        this.bot = bot;

        mapCommands = new HashMap<>();
        mapCommands.put(Commands.start, this::start);
    }

    public Runnable getCommand(String command) {
        Commands commandKey;
        try {
            commandKey = Commands.fromString(command);
            return mapCommands.get(commandKey);
        } catch (NotCommandException exception) {
            LogConfig.logWarn(exception.getMessage());
            return this::notACommand;
        }
    }

    public Command setByUpdate(Update update) {
        id = update.message().chat().id();
        firstName = update.message().from().firstName();
        return this;
    }

    private void start() {
        SendMessage message = new SendMessage(id, String.format("""
                Привет, %s! Я бот-помощник доктора Баймуканова.%n
                Выберите интересующий вас пункт.
                """, firstName
        ));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Пригласить друга").callbackData(Buttons.inviteFriend.name()),
                new InlineKeyboardButton("Получить гайд").callbackData(Buttons.getGuide.name())
        );

        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Оценить сердечно-сосудистый риск").callbackData(Buttons.assessRisks.name()));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Записаться на консультацию").callbackData(Buttons.makeAppointment.name()));

        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Обо мне").callbackData(Buttons.aboutMe.name()),
                new InlineKeyboardButton("Помощь с ботом").callbackData(Buttons.help.name())
        );

        message.replyMarkup(inlineKeyboardMarkup);
        bot.execute(message);
    }

    private void notACommand() {
        bot.execute(new SendMessage(
                id,
                """
                        Кажется, Вы ввели неправильную команду.
                        Ничего страшного, попробуйте еще раз!
                        Чтобы все правильно сработало, нажмите кнопку меню внизу экрана, и Вы увидите список доступных команд.
                        """
        ));
    }
}
