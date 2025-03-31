package com.example.cardiotelegrambot.service.bot.main;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.config.enums.main.Buttons;
import com.example.cardiotelegrambot.entity.ReviewEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchReviewException;
import com.example.cardiotelegrambot.exceptions.NotMemberException;
import com.example.cardiotelegrambot.exceptions.ReviewExistException;
import com.example.cardiotelegrambot.service.database.ReviewService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.MessagesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Button {

    private final TelegramBot bot;
    private final ReviewService reviewService;
    private final Logger logger;
    private final ConfigurableApplicationContext context;

    private Long chatId;
    private Integer messageId;
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
    public Button(@Qualifier("mainBotBean") TelegramBot bot, ReviewService reviewService, Logger logger, ConfigurableApplicationContext context) {
        this.bot = bot;
        this.reviewService = reviewService;
        this.logger = logger;
        this.context = context;

        buttons = new HashMap<>();
        buttons.put(Buttons.inviteFriend, this::inviteFriend);
        buttons.put(Buttons.getGuide, this::getGuide);
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

        username = update
                .callbackQuery()
                .from()
                .username();

        return this;
    }

    public Button setByVariables(Long chatId) {
        this.username = null;
        this.chatId = chatId;

        return this;
    }

    private void inviteFriend() {
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                Скопируйте и отправьте ссылку другу:
                
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
            logger.logException(exception);
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
                    "Review for user \"%s\"_%s was added to database.",
                    username,
                    chatId
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
            ReviewEntity review = reviewService.getReview(chatId);

            for (Integer messageId : review.getMessageIds()) {
                bot.execute(new DeleteMessage(
                        review.getChatId(),
                        messageId
                ));
            }

            reviewService.deleteReview(chatId);
            logger.logInfo(String.format(
                    "Review for user \"%s\"_%s was deleted from database.",
                    username,
                    chatId
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
                new InputMediaPhoto(getFileFromResources("/03302024-2221.png")),
                new InputMediaPhoto(getFileFromResources("/07262024-1829.png")),
                new InputMediaPhoto(getFileFromResources("/07282024-1204.png")),
                new InputMediaPhoto(getFileFromResources("/08052024-1353.png")),
                new InputMediaPhoto(getFileFromResources("/08062024-1214.png"))
        ));
        bot.execute(new DeleteMessage(chatId, messageId));
        createReview(response.messages());

        SendMessage message = new SendMessage(chatId, String.format("""
                Несколько отзывов с проверенного сайта 😉
                Хотите еще? Переходите по ссылке ⏬️️️
                %n%s
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
                Образование и карьера доктора Баймуканова⏬️️️
                %n%s
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
                Опыт и экспертность доктора Баймуканова.
                
                Выберите, что вас интересует ⏬️️
                """
        );

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Образование").callbackData(Buttons.education.name()),
                new InlineKeyboardButton("Отзывы").callbackData(Buttons.reviews.name())
        );
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        );

        message.replyMarkup(inlineKeyboardMarkup);
        bot.execute(message);
    }

    private void makeAppointment() {
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                Оставьте заявку на онлайн-консультацию ⏬️️
                
                В течение 24ч с вами свяжутся и согласуют удобное время.
                %n%s
                """, makeAppointmentLink
        ));

        message.replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        ));
        bot.execute(message);
    }

    private void help() {
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                Я — бот-новичок и могу делать ошибки. Напишите моему создателю %s, помогите мне стать лучше 😇
                """, creatorUsername
        ));

        message.replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
        ));
        bot.execute(message);
    }

    private void getBack() {
        deleteReview();
        EditMessageText message = new EditMessageText(chatId, messageId, """
                И снова на связи я, бот-помощник Азамата Баймуканова 👋
                
                Выберите, что вас интересует ⏬️️
                """
        );

        message.replyMarkup(context
                .getBean(Command.class)
                .getInlineKeyboardMarkupForMainMenu()
        );
        bot.execute(message);
    }

    public void isSubscribed() throws NotMemberException {
        ChatMember.Status status = bot.execute(new GetChatMember(channelId, chatId))
                .chatMember()
                .status();

        if (!(
                status == ChatMember.Status.creator ||
                status == ChatMember.Status.administrator ||
                status == ChatMember.Status.member
        )) {
            throw new NotMemberException(username, chatId);
        }
    }

    private void getGuide() {
        try {
            isSubscribed();

            EditMessageText message = new EditMessageText(
                    chatId,
                    messageId,
                    String.format("""
                            Экспертный гайд Азамата Баймуканова
                            "<a href="%s">Как сохранить здоровье сердца после 30 лет</a>".
                            
                            Скачать ⏫️
                            """, linkToFile)
            ).parseMode(ParseMode.HTML);

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
                            Упс! Попробуем еще раз! Подпишитесь на канал
                            
                            <a href="https://t.me/%s">Заметки Кардиолога</a> ⏬️️
                            """,
                            channelUsername)
            ).parseMode(ParseMode.HTML);

            message.replyMarkup(new InlineKeyboardMarkup().addRow(
                    new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name()),
                    new InlineKeyboardButton("Я подписался").callbackData(Buttons.getGuide.name())
            ));
            bot.execute(message);
        }
    }
}
