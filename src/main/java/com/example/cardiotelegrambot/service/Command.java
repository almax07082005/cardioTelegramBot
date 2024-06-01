package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.config.enums.Buttons;
import com.example.cardiotelegrambot.config.enums.Commands;
import com.example.cardiotelegrambot.entity.UserEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchUserException;
import com.example.cardiotelegrambot.exceptions.NotCommandException;
import com.example.cardiotelegrambot.exceptions.UserExistException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class Command {

    private final TelegramBot bot;
    private final UserService userService;
    private final Logger logger;

    private Long chatId;
    private Integer messageId;
    private String firstName;
    private String username;
    private String referralLink;
    private final Map<Commands, Runnable> mapCommands;

    @Autowired
    public Command(@Qualifier("mainBotBean") TelegramBot bot, UserService userService, Logger logger) {
        this.bot = bot;
        this.userService = userService;
        this.logger = logger;

        mapCommands = new HashMap<>();
        mapCommands.put(Commands.start, this::start);
    }

    public Runnable getCommand(String command) {
        try {
            Pair<Commands, String> pair = Commands.fromString(command);
            referralLink = pair.getSecond();
            return mapCommands.get(pair.getFirst());

        } catch (NotCommandException exception) {
            logger.logWarn(String.format(
                    "\"%s\": %s",
                    username,
                    exception.getMessage()
            ));
            return this::notACommand;
        }
    }

    public Command setByUpdate(Update update) {
        chatId = update
                .message()
                .chat()
                .id();

        messageId = update
                .message()
                .messageId();

        firstName = update
                .message()
                .from()
                .firstName();

        username = update
                .message()
                .from()
                .username();

        return this;
    }

    public static InlineKeyboardMarkup getInlineKeyboardMarkupForMainMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Пригласить друга").callbackData(Buttons.inviteFriend.name()),
                new InlineKeyboardButton("Получить гайд").callbackData(Buttons.getGuide.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Оценить сердечно-сосудистый риск").callbackData(Buttons.assessRisks.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Записаться на консультацию").callbackData(Buttons.makeAppointment.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Обо мне").callbackData(Buttons.aboutMe.name()),
                new InlineKeyboardButton("Помощь с ботом").callbackData(Buttons.help.name())
        );

        return inlineKeyboardMarkup;
    }

    private void updateLinkSender() {

        if (referralLink.isBlank()) {
            return;
        }

        try {
            UserEntity user = userService.getByChatId(Long.valueOf(referralLink));

            Set<Long> usersChatIds = user.getUsersChatIds();
            usersChatIds.add(chatId);
            user.setUsersChatIds(usersChatIds);

            userService.updateUser(user);
            logger.logInfo(String.format(
                    "To user \"%s\" referral link was added in database.",
                    user.getUsername()
            ));

        } catch (NumberFormatException | NoSuchUserException exception) {
            logger.logWarn(String.format(
                    "User \"%s\" has an invalid referral link.",
                    username
            ));
        }
    }

    private void start() {
        updateLinkSender();

        SendMessage message = new SendMessage(chatId, String.format("""
                Здравствуйте, %s! Я бот-помощник кардиолога Азамата Баймуканова.%n
                Выберите интересующий вас пункт меню.
                """, firstName
        ));

        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());
        SendResponse response = bot.execute(message);
        notACommand();

        UserEntity newUser = UserEntity.builder()
                .username(username)
                .chatId(response.message().chat().id())
                .messageId(response.message().messageId())
                .build();

        try {
            UserEntity user = userService.getByUsername(username);
            bot.execute(new DeleteMessage(
                    user.getChatId(),
                    user.getMessageId()
            ));
            userService.updateUser(newUser);
            logger.logInfo(String.format(
                    "User \"%s\" was updated in database.",
                    username
            ));

        } catch (NoSuchUserException exception) {
            try {
                userService.createUser(newUser);
                logger.logInfo(String.format(
                        "User \"%s\" was added to database.",
                        username
                ));
            } catch (UserExistException nestedException) {
                logger.logError(String.format(
                        "%s (unpredictable behavior)",
                        nestedException.getMessage()
                ));
            }
        }
    }

    private void notACommand() {
        bot.execute(new DeleteMessage(
                chatId,
                messageId
        ));
    }
}
