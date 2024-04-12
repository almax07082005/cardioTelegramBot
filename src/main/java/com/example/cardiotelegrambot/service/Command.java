package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.config.BotConfig;
import com.example.cardiotelegrambot.config.LogConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.GetMyCommands;
import com.pengrad.telegrambot.request.SendMessage;
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
            bot.execute(new GetChatMember(
                    context.getBean(BotConfig.class).getChannelId(),
                    id
            ));
            bot.execute(new SendMessage(
                    id,
                    String.format("""
                            А вот и ваш гайд! (доступен по ссылке на Яндекс диске)
                            %s
                            """, linkToFile)
            ));
        } catch (NullPointerException exception) {
            LogConfig.logError(exception.getStackTrace());
            bot.execute(new SendMessage(
                    id,
                    """
                            Извините, но кажется Вы не подписаны на наш канал.
                            Проверьте, подписаны ли Вы, там много полезной информации!
                            Если Вы уже проверили и все равно не работает, выберите команду /help и напишите мне, я обязательно Вам помогу!
                            """
            ));
        }
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
                Чтобы все правильно сработало, нажмите кнопку меню внизу экрана, и Вы увидите список доступных команд.
                """);
        bot.execute(message);
    }
}
