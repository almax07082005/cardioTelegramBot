package com.example.cardiotelegrambot.service.bot.main;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.config.enums.main.Buttons;
import com.example.cardiotelegrambot.config.enums.main.Commands;
import com.example.cardiotelegrambot.entity.UserEntity;
import com.example.cardiotelegrambot.exceptions.AlreadyReferralException;
import com.example.cardiotelegrambot.exceptions.NoSuchUserException;
import com.example.cardiotelegrambot.exceptions.NotCommandException;
import com.example.cardiotelegrambot.exceptions.SelfReferralException;
import com.example.cardiotelegrambot.exceptions.UserExistException;
import com.example.cardiotelegrambot.service.database.ReferralService;
import com.example.cardiotelegrambot.service.database.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    private final ReferralService referralService;

    private Long chatId;
    private Integer messageId;
    private String firstName;
    private String username;
    private String referralLink;
    private final Map<Commands, Runnable> mapCommands;

    @Value("${telegram.channel.username}")
    private String channelUsername;

    @Autowired
    public Command(@Qualifier("mainBotBean") TelegramBot bot, UserService userService, Logger logger, ReferralService referralService) {
        this.bot = bot;
        this.userService = userService;
        this.logger = logger;
        this.referralService = referralService;

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

    public InlineKeyboardMarkup getInlineKeyboardMarkupForMainMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Подписаться на канал").callbackData(Buttons.subscribe.name()).url(String.format("https://t.me/%s", channelUsername))
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Вступить в закрытый чат").callbackData(Buttons.joinPrivateChat.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Пригласить друга").callbackData(Buttons.inviteFriend.name()),
                new InlineKeyboardButton("Получить гайд").callbackData(Buttons.getGuide.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Записаться на консультацию").callbackData(Buttons.makeAppointment.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("О докторе").callbackData(Buttons.aboutMe.name()),
                new InlineKeyboardButton("Помощь с ботом").callbackData(Buttons.help.name())
        );

        return inlineKeyboardMarkup;
    }

    private class Response {

        String selfReferral() {
            return """
                    Упс! Вы перешли по своей же реферальной ссылке. Отправьте приглашение другу снова 😊
                    """;
        }

        String success() {
            return String.format("""
                    Приветствую%s
                    Перейдя по ссылке, вы зарегистрировались в реферальной программе! Осталось только <strong>ПОДПИСАТЬСЯ НА КАНАЛ</strong> ⏬️️
                    """, (isRegistered() ? " еще раз!\n" : String.format("""
                    , %s!
                    Я — бот-помощник кардиолога Азамата Баймуканова.
                    """, firstName))
            );
        }

        String noReferral() {
            return String.format("""
                    Приветствую, %s!
                    Я — бот-помощник кардиолога Азамата Баймуканова.
                    
                    Выберите, что вас интересует ⏬️
                    """, firstName
            );
        }

        String incorrectReferral() {
            return String.format("""
                    Приветствую%s
                    Ошибочка вышла! Перейдите по ссылке еще раз 😊
                    """, (isRegistered() ? " еще раз!\n" : String.format("""
                    , %s!
                    Я — бот-помощник кардиолога Азамата Баймуканова.
                    """, firstName))
            );
        }

        String alreadyReferral() {
            return """
                    Приветствую еще раз!
                    
                    А вы уже зарегистрированы в реферальной программе 😉
                    
                    Выберите, что вас интересует ⏬️️
                    """;
        }

        private Boolean isRegistered() {
            try {
                userService.getByChatId(chatId);
                return true;
            } catch (NoSuchUserException exception) {
                return false;
            }
        }
    }

    private Pair<String, Boolean> updateLinkSender() {
        Response response = new Response();

        boolean isReferral;
        try {
            isReferral = userService.getByChatId(chatId).getIsReferral();
        } catch (NoSuchUserException ignored) {
            isReferral = false;
        }

        if (referralLink.isBlank()) {
            return Pair.of(response.noReferral(), isReferral);
        }
        if (!referralService.isPresent()) {
            logger.logWarn(String.format(
                    "User \"%s\"_%s tried to use unstarted referral program.",
                    username,
                    chatId
            ));
            return Pair.of(response.noReferral(), isReferral);
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
            return Pair.of(response.success(), true);

        } catch (NumberFormatException | NoSuchUserException ignored) {
            logger.logWarn(String.format(
                    "User \"%s\"_%s has an invalid referral link.",
                    username,
                    chatId
            ));
            return Pair.of(response.incorrectReferral(), isReferral);

        } catch (AlreadyReferralException exception) {
            logger.logWarn(exception.getMessage());
            return Pair.of(response.alreadyReferral(), true);

        } catch (SelfReferralException exception) {
            logger.logWarn(exception.getMessage());
            return Pair.of(response.selfReferral(), false);
        }
    }

    private void start() {
        Pair<String, Boolean> isReferralPair = updateLinkSender();

        SendMessage message = new SendMessage(chatId, isReferralPair.getFirst()).parseMode(ParseMode.HTML);
        message.replyMarkup(getInlineKeyboardMarkupForMainMenu());
        SendResponse response = bot.execute(message);
        notACommand();

        UserEntity.UserEntityBuilder newUser = UserEntity.builder()
                .username(username)
                .chatId(response.message().chat().id())
                .messageId(response.message().messageId())
                .isReferral(isReferralPair.getSecond());
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

        } catch (NoSuchUserException ignored) {
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
