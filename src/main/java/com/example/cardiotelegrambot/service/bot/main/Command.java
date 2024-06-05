package com.example.cardiotelegrambot.service.bot.main;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.config.enums.Buttons;
import com.example.cardiotelegrambot.config.enums.Commands;
import com.example.cardiotelegrambot.entity.UserEntity;
import com.example.cardiotelegrambot.exceptions.AlreadyReferralException;
import com.example.cardiotelegrambot.exceptions.NoSuchUserException;
import com.example.cardiotelegrambot.exceptions.NotCommandException;
import com.example.cardiotelegrambot.exceptions.SelfReferralException;
import com.example.cardiotelegrambot.exceptions.UserExistException;
import com.example.cardiotelegrambot.service.database.UserService;
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

import java.io.FileReader;
import java.io.IOException;
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
                    "\"%s\"_%s: %s",
                    username,
                    chatId,
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

    private String read(FileReader reader) throws IOException {
        char[] buffer = new char[5];
        int charactersRead = reader.read(buffer, 0, 5);
        if (charactersRead != -1) {
            return new String(buffer, 0, charactersRead);
        } else {
            return "";
        }
    }

    private boolean updateLinkSender() {

        boolean isProgramStarted;
        try (FileReader fileReader = new FileReader("referral.txt")) {
            isProgramStarted = Boolean.parseBoolean(read(fileReader));
        } catch (IOException exception) {
            isProgramStarted = false;
        }

        boolean isReferral;
        try {
            isReferral = userService.getByChatId(chatId).getIsReferral();
        } catch (NoSuchUserException exception) {
            isReferral = false;
        }

        if (referralLink.isBlank() || !isProgramStarted) {
            return isReferral;
        }
        try {
            if (isReferral) {
                throw new AlreadyReferralException(username, chatId);
            }

            Long longReferralLink = Long.valueOf(referralLink);
            if (longReferralLink.equals(chatId)) {
                throw new SelfReferralException(username, chatId);
            }

            UserEntity user = userService.getByChatId(longReferralLink);
            Set<Long> usersChatIds = user.getUsersChatIds();

            usersChatIds.add(chatId);
            user.setUsersChatIds(usersChatIds);

            userService.updateUser(user);
            logger.logInfo(String.format(
                    "To user \"%s\"_%s referral link was added in database.",
                    user.getUsername(),
                    user.getChatId()
            ));
            return true;

        } catch (NumberFormatException | NoSuchUserException exception) {
            logger.logWarn(String.format(
                    "User \"%s\"_%s has an invalid referral link.",
                    username,
                    chatId
            ));
            return isReferral;

        } catch (SelfReferralException | AlreadyReferralException exception) {
            logger.logWarn(exception.getMessage());
            return isReferral;
        }
    }

    private void start() {
        boolean isReferral = updateLinkSender();
        SendMessage message = new SendMessage(chatId, String.format("""
                Здравствуйте, %s! Я бот-помощник кардиолога Азамата Баймуканова.%n
                Выберите интересующий вас пункт меню.
                """, firstName
        ));

        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());
        SendResponse response = bot.execute(message);
        notACommand();

        UserEntity.UserEntityBuilder newUser = UserEntity.builder()
                .username(username)
                .chatId(response.message().chat().id())
                .messageId(response.message().messageId())
                .isReferral(isReferral);
        try {
            UserEntity user = userService.getByChatId(chatId);
            bot.execute(new DeleteMessage(
                    user.getChatId(),
                    user.getMessageId()
            ));
            userService.updateUser(newUser
                    .usersChatIds(user.getUsersChatIds())
                    .build()
            );
            logger.logInfo(String.format(
                    "User \"%s\"_%s was updated in database.",
                    username,
                    chatId
            ));

        } catch (NoSuchUserException exception) {
            try {
                userService.createUser(newUser.build());
                logger.logInfo(String.format(
                        "User \"%s\"_%s was added to database.",
                        username,
                        chatId
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
