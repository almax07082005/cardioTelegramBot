package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.config.enums.Buttons;
import com.example.cardiotelegrambot.entity.ReviewEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchReviewException;
import com.example.cardiotelegrambot.exceptions.NotMemberException;
import com.example.cardiotelegrambot.exceptions.ReviewExistException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.MessagesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PropertySource("classpath:hidden.properties")
@Component
public class Button {

    private final TelegramBot bot;
    private final ReviewService reviewService;
    private final Logger logger;

    private Long chatId;
    private Integer messageId;
    private String firstName;
    private String username;
    private final Map<Buttons, Runnable> buttons;

    @Value("${telegram.channel.username}")
    private String channelUsername;

    @Value("${telegram.channel.id}")
    private Long channelId;

    @Value("${telegram.guide.file}")
    private String linkToFile;

    @Value("${telegram.creator.username}")
    private String creatorUsername;

    @Value("${telegram.assessRisks.link}")
    private String assessRisksLink;

    @Value("${telegram.makeAppointment.link}")
    private String makeAppointmentLink;

    @Value("${telegram.reviews.link}")
    private String reviewsLink;

    @Value("${telegram.education.link}")
    private String educationLink;

    @Autowired
    public Button(@Qualifier("mainBotBean") TelegramBot bot, ReviewService reviewService, Logger logger) {
        this.bot = bot;
        this.reviewService = reviewService;
        this.logger = logger;

        buttons = new HashMap<>();
        buttons.put(Buttons.inviteFriend, this::inviteFriend);
        buttons.put(Buttons.getGuide, this::getGuide);
        buttons.put(Buttons.assessRisks, this::assessRisks);
        buttons.put(Buttons.makeAppointment, this::makeAppointment);
        buttons.put(Buttons.aboutMe, this::aboutMe);
        buttons.put(Buttons.help, this::help);
        buttons.put(Buttons.getBack, this::getBack);
        buttons.put(Buttons.reviews, this::reviews);
        buttons.put(Buttons.education, this::education);
    }

    public Runnable getButton(Buttons button) {
        return buttons.get(button);
    }

    public Button setByUpdate(Update update) {
        chatId = update
                .callbackQuery()
                .from()
                .id();

        messageId = update
                .callbackQuery()
                .message()
                .messageId();

        firstName = update
                .callbackQuery()
                .from()
                .firstName();

        username = update
                .callbackQuery()
                .from()
                .username();

        return this;
    }

    private void inviteFriend() {
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                А вот и Ваша реферальная ссылка:
                https://t.me/cardiozametki_bot?start=%s
                """,
                chatId
        ));

        message.replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        ));
        bot.execute(message);
    }

    private byte[] getFileFromResources(String fileName) {
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new FileNotFoundException("File not found " + fileName);
            }

            return is.readAllBytes();
        } catch (IOException exception) {
            logger.logError(exception);
            return null;
        }
    }

    private void createReview(Message[] messages) {
        List<Integer> messageIds = new ArrayList<>();
        for (Message message : messages) {
            messageIds.add(message.messageId());
        }

        try {
            reviewService.createReview(ReviewEntity.builder()
                    .username(username)
                    .chatId(chatId)
                    .messageIds(messageIds)
                    .build()
            );
            logger.logInfo(String.format(
                    "Review for user \"%s\" was added to database.",
                    username
            ));

        } catch (ReviewExistException exception) {
            logger.logError(String.format(
                    "%s (unpredictable behavior)",
                    exception.getMessage()
            ));
        }
    }

    private void deleteReview() {
        try {
            ReviewEntity review = reviewService.getReview(username);

            for (Integer messageId : review.getMessageIds()) {
                bot.execute(new DeleteMessage(
                        review.getChatId(),
                        messageId
                ));
            }

            reviewService.deleteReview(username);
            logger.logInfo(String.format(
                    "Review for user \"%s\" was deleted from database.",
                    username
            ));

        } catch (NoSuchReviewException ignored) {}
    }

    private void reviews() {
        MessagesResponse response = bot.execute(new SendMediaGroup(
                chatId,
                new InputMediaPhoto(getFileFromResources("/11012023-1416.png")),
                new InputMediaPhoto(getFileFromResources("/02242024-1947.png")),
                new InputMediaPhoto(getFileFromResources("/03052024-1018.png")),
                new InputMediaPhoto(getFileFromResources("/03172024-0735.png")),
                new InputMediaPhoto(getFileFromResources("/03302024-2221.png"))
        ));
        bot.execute(new DeleteMessage(chatId, messageId));
        createReview(response.messages());

        SendMessage message = new SendMessage(chatId, String.format("""
                Вот несколько отзывов с проверенного сайта!
                С остальными отзывами можно ознакомиться здесь:
                %s
                """, reviewsLink
        ));

        message.replyMarkup(new InlineKeyboardMarkup().addRow(
                new InlineKeyboardButton("Назад").callbackData(Buttons.aboutMe.name()),
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        ));
        bot.execute(message);
    }

    private void education() {
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                Здесь всё об образовании Баймуканова Азамата.
                Ознакомиться по ссылке:
                %s
                """, educationLink
        ));

        message.replyMarkup(new InlineKeyboardMarkup().addRow(
                new InlineKeyboardButton("Назад").callbackData(Buttons.aboutMe.name()),
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        ));
        bot.execute(message);
    }

    private void aboutMe() {
        deleteReview();
        EditMessageText message = new EditMessageText(chatId, messageId, """
                Здесь информация обо мне.
                Выберите интересующее Вас меню.
                """
        );

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Отзывы").callbackData(Buttons.reviews.name()),
                new InlineKeyboardButton("Образование").callbackData(Buttons.education.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        );

        message.replyMarkup(inlineKeyboardMarkup);
        bot.execute(message);
    }

    private void makeAppointment() {
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                Записаться на консультацию Вы можете здесь:
                %s
                """, makeAppointmentLink
        ));

        message.replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        ));
        bot.execute(message);
    }

    private void assessRisks() {
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                Оценить свой сердечно-сосудистый риск Вы сможете здесь:
                %s
                """, assessRisksLink
        ));

        message.replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        ));
        bot.execute(message);
    }

    private void help() {
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                Сожалеем, что у Вас что-то не работает так, как нужно.
                С любыми вопросами Вы можете обратиться к моему создателю.
                А вот и его аккаунт: %s
                """, creatorUsername
        ));

        message.replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        ));
        bot.execute(message);
    }

    private void getBack() {
        deleteReview();
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                И снова здравствуйте, %s! Опять я, бот-помощник кардиолога Азамата Баймуканова.%n
                Выберите интересующий вас пункт меню.
                """, firstName
        ));

        message.replyMarkup(Command.getInlineKeyboardMarkupForMainMenu());
        bot.execute(message);
    }

    private void getGuide() {
        try {
            ChatMember.Status status = bot.execute(new GetChatMember(channelId, chatId))
                    .chatMember()
                    .status();

            if (!(
                    status == ChatMember.Status.creator ||
                    status == ChatMember.Status.administrator ||
                    status == ChatMember.Status.member
            )) {
                throw new NotMemberException(username);
            }

            EditMessageText message = new EditMessageText(
                    chatId,
                    messageId,
                    String.format("""
                            А вот и Ваш гайд! (доступен по ссылке на Яндекс диске)
                            %s
                            """, linkToFile)
            );

            message.replyMarkup(new InlineKeyboardMarkup(
                    new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
            ));
            bot.execute(message);

        } catch (NotMemberException exception) {
            logger.logWarn(exception.getMessage());

            EditMessageText message = new EditMessageText(
                    chatId,
                    messageId,
                    String.format("""
                            Извините, но Вы не подписаны на канал "Заметки Кардиолога".
                            Проверьте, подписаны ли Вы на канал %s, там много полезной информации!
                            """, channelUsername)
            );

            message.replyMarkup(new InlineKeyboardMarkup().addRow(
                    new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name()),
                    new InlineKeyboardButton("Я подписался").callbackData(Buttons.getGuide.name())
            ));
            bot.execute(message);
        }
    }
}
