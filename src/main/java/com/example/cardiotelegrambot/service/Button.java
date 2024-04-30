package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.LogConfig;
import com.example.cardiotelegrambot.config.enums.Buttons;
import com.example.cardiotelegrambot.exceptions.NotMemberException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetChatMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@PropertySource("classpath:hidden.properties")
@Component
public class Button {

    private final TelegramBot bot;

    private Long chatId;
    private Integer messageId;
    private String firstName;
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

    @Autowired
    public Button(TelegramBot bot) {
        this.bot = bot;

        buttons = new HashMap<>();
        buttons.put(Buttons.inviteFriend, null);
        buttons.put(Buttons.getGuide, this::getGuide);
        buttons.put(Buttons.assessRisks, this::assessRisks);
        buttons.put(Buttons.makeAppointment, this::makeAppointment);
        buttons.put(Buttons.aboutMe, null);
        buttons.put(Buttons.help, this::help);
        buttons.put(Buttons.getBack, this::getBack);
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

        return this;
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
                Оценить свой риск сердечно-сосудистых заболеваний Вы можете здесь:
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
        EditMessageText message = new EditMessageText(chatId, messageId, String.format("""
                Привет, %s! Я бот-помощник доктора Баймуканова.%n
                Выберите интересующий вас пункт.
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

            if (!(status == ChatMember.Status.creator || status == ChatMember.Status.administrator || status == ChatMember.Status.member)) {
                throw new NotMemberException();
            }

            EditMessageText message = new EditMessageText(
                    chatId,
                    messageId,
                    String.format("""
                            А вот и ваш гайд! (доступен по ссылке на Яндекс диске)
                            %s
                            """, linkToFile)
            );

            message.replyMarkup(new InlineKeyboardMarkup(
                    new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
            ));
            bot.execute(message);

        } catch (NotMemberException exception) {
            LogConfig.logWarn(exception.getMessage());

            EditMessageText message = new EditMessageText(
                    chatId,
                    messageId,
                    String.format("""
                            Извините, но, кажется, Вы не подписаны на наш канал.
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
