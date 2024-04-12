package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.BotConfig;
import com.example.cardiotelegrambot.config.LogConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:hidden.properties")
public class Command {

    private final TelegramBot bot;
    private final ConfigurableApplicationContext context;
    private Long id;

    @Value("${telegram.guide.file}")
    private String linkToFile;

    @Value("${telegram.creator.alias}")
    private String alias;

    @Autowired
    public Command(TelegramBot bot, ConfigurableApplicationContext context) {
        this.bot = bot;
        this.context = context;
    }

    public Command setByUpdate(Update update) {
        id = update.message().chat().id();
        return this;
    }

    public void start() {
        // TODO Get list of commands
        SendMessage message = new SendMessage(id, """
                Привет, я бот Максим и могу помочь с чем угодно!
                Вот список доступных команд:
                /start - начать новый диалог;
                /guide - получить гид;
                /help - получить помощь.
                """);
        bot.execute(message);
    }

    public void guide() {
        // TODO Understand, why info gives false
        GetChatMemberResponse response = bot.execute(new GetChatMember(context.getBean(BotConfig.class).getChannelId(), id));
        LogConfig.logInfo(response.chatMember().isMember());

        SendMessage message = new SendMessage(id, String.format("""
                А вот и ваш гид! (доступен по ссылке на Яндекс диске)
                %s
                """, linkToFile));
        bot.execute(message);
    }

    public void help() {
        SendMessage message = new SendMessage(id, String.format("""
                Если Вам нужна помощь, Вы можете обратиться к создателю этого бота!
                Вот его аккаунт: %s.
                """, alias));
        bot.execute(message);
    }

    public void notACommand() {
        SendMessage message = new SendMessage(id, """
                Кажется, Вы ввели неправильную команду.
                Ничего страшного, попробуйте еще раз!
                """);
        bot.execute(message);
    }
}
