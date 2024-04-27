package com.example.cardiotelegrambot;

import com.example.cardiotelegrambot.service.BotService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CardioTelegramBotApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CardioTelegramBotApplication.class, args);
        context.getBean(BotService.class).startBot();
    }
}
