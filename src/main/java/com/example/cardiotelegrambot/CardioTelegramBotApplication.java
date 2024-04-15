package com.example.cardiotelegrambot;

import com.example.cardiotelegrambot.service.Bot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is the main class for the CardioTelegramBot application.
 * It uses the Spring Boot framework for creating stand-alone, production-grade Spring based applications.
 * It provides a main method that starts the application and the bot.
 */
@SpringBootApplication
public class CardioTelegramBotApplication {

    /**
     * This is the main method for the CardioTelegramBot application.
     * It starts the application and the bot.
     * It takes an array of strings as input, which are the arguments for the application.
     * It creates a ConfigurableApplicationContext object by running the application with the given arguments.
     * It then gets a Bot object from the application context and starts the bot.
     *
     * @param args An array of strings representing the arguments for the application.
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CardioTelegramBotApplication.class, args);
        context.getBean(Bot.class).startBot();
    }
}
