package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.LogConfig;
import com.example.cardiotelegrambot.config.enums.Buttons;
import com.example.cardiotelegrambot.config.enums.Commands;
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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@PropertySource("classpath:hidden.properties")
@Component
public class Button {

    private final TelegramBot bot;
    private final ConfigurableApplicationContext context;

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

    @Autowired
    public Button(TelegramBot bot, ConfigurableApplicationContext context) {
        this.bot = bot;
        this.context = context;

        buttons = new HashMap<>();
        buttons.put(Buttons.inviteFriend, null);
        buttons.put(Buttons.getGuide, this::getGuide);
        buttons.put(Buttons.assessRisks, null);
        buttons.put(Buttons.makeAppointment, null);
        buttons.put(Buttons.aboutMe, null);
        buttons.put(Buttons.help, null);
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

    private void getBack() {
        context.getBean(Command.class)
                .setByVariables(
                        chatId,
                        messageId,
                        firstName
                )
                .getCommand(Commands.start.toString())
                .run();
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
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
                    new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name())
            );

            message.replyMarkup(inlineKeyboardMarkup);
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
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup().addRow(
                    new InlineKeyboardButton("Главное меню").callbackData(Buttons.getBack.name()),
                    new InlineKeyboardButton("Я подписался").callbackData(Buttons.getGuide.name())
            );

            message.replyMarkup(inlineKeyboardMarkup);
            bot.execute(message);
        }
    }
}
