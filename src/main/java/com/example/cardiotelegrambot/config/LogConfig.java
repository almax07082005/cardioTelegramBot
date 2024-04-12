package com.example.cardiotelegrambot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@Slf4j
public abstract class LogConfig {

    public static void logError(StackTraceElement[] stackTraceElements) {
        log.error(String.join("\n", Arrays.toString(stackTraceElements).split(" ")));
    }

    public static <T> void logInfo(T info) {
        try {
            log.info(info.toString());
        } catch (NullPointerException exception) {
            LogConfig.logError(exception.getStackTrace());
        }
    }
}
