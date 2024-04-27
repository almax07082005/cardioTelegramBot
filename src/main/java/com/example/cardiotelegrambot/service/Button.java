package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.enums.Buttons;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
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
        buttons.put(Buttons.getGuide, null);
        buttons.put(Buttons.assessRisks, null);
        buttons.put(Buttons.makeAppointment, null);
        buttons.put(Buttons.aboutMe, null);
        buttons.put(Buttons.help, null);
    }

    public Runnable getButton(Buttons button) {
        return buttons.get(button);
    }

    public Button setByUpdate(Update update) {
        chatId = update
                .callbackQuery()
                .from()
                .id();
        return this;
    }
}
