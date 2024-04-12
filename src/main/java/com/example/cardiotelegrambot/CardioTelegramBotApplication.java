package com.example.cardiotelegrambot;

import com.example.cardiotelegrambot.service.Bot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CardioTelegramBotApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CardioTelegramBotApplication.class, args);
        context.getBean(Bot.class).startBot();
    }
}
