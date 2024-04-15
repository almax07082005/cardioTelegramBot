package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.BotConfig;
import com.example.cardiotelegrambot.config.LogConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.GetMyCommands;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@PropertySource("classpath:hidden.properties")
public class Command {

    private final TelegramBot bot;
    private final ConfigurableApplicationContext context;
    private Long id;

    @Getter
    private final Map<String, Runnable> mapCommands;

    @Value("${telegram.guide.file}")
    private String linkToFile;

    @Value("${telegram.creator.alias}")
    private String creatorAlias;

    @Value("${telegram.channel.alias}")
    private String channelAlias;

    @Autowired
    public Command(TelegramBot bot, ConfigurableApplicationContext context) {
        this.bot = bot;
        this.context = context;

        mapCommands = new HashMap<>();
        mapCommands.put("/start", this::start);
        mapCommands.put("/guide", this::guide);
        mapCommands.put("/help", this::help);
    }

    public Command setByUpdate(Update update) {
        id = update.message().chat().id();
        return this;
    }

    public void start() {
        StringBuilder text = new StringBuilder("""
                Привет, я Кардио Бот и помогу Вам!
                Вот список доступных команд:
                """);
        BotCommand[] commands = bot.execute(new GetMyCommands()).commands();

        for (int i = 0; i < commands.length; i++) {
            BotCommand command = commands[i];
            text.append("/")
                    .append(command.command())
                    .append(" - ")
                    .append(command.description());

            if (i + 1 == commands.length) text.append(".");
            else text.append(";\n");
        }

        bot.execute(new SendMessage(id, text.toString()));
    }

    public void guide() {
        try {
            ChatMember.Status status = bot.execute(new GetChatMember(
                    context.getBean(BotConfig.class).getChannelId(),
                    id
            )).chatMember().status();

            if (!(status == ChatMember.Status.creator || status == ChatMember.Status.administrator || status == ChatMember.Status.member)) {
                throw new NullPointerException("Current user is not a member");
            }

            bot.execute(new SendMessage(
                    id,
                    String.format("""
                            А вот и ваш гайд! (доступен по ссылке на Яндекс диске)
                            %s
                            """, linkToFile)
            ));
        } catch (Exception exception) {
            LogConfig.logError(exception.getStackTrace());
            bot.execute(new SendMessage(
                    id,
                    String.format("""
                            Извините, но, кажется, Вы не подписаны на наш канал.
                            Проверьте, подписаны ли Вы на канал %s, там много полезной информации!
                            Если Вы уже проверили и все равно не работает, выберите команду /help и напишите мне, я обязательно Вам помогу!
                            """, channelAlias)
            ));
        }
    }

    public void help() {
        bot.execute(new SendMessage(
                id,
                String.format("""
                        Если Вам нужна помощь, Вы можете обратиться к создателю этого бота!
                        Вот его аккаунт: %s.
                        """, creatorAlias)));
    }

    public void notACommand() {
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
