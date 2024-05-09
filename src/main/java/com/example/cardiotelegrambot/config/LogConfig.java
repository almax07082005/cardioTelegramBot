package com.example.cardiotelegrambot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@Slf4j
public abstract class LogConfig {

    public static void logError(Exception exception) {
        log.error("{}\n{}",
                exception.getMessage(),
                String.join("\n", Arrays
                        .toString(exception.getStackTrace())
                        .split(" ")
        ));
    }

    public static void logWarn(Exception exception) {
        log.warn("{}\n{}",
                exception.getMessage(),
                String.join("\n", Arrays
                        .toString(exception.getStackTrace())
                        .split(" ")
        ));
    }

    public static <T> void logWarn(T warn) {
        try {
            log.warn(warn.toString());
        } catch (Exception ignored) {
            log.warn("No info message provided");
        }
    }

    public static <T> void logInfo(T info) {
        try {
            log.info(info.toString());
        } catch (Exception ignored) {
            log.info("No info message provided");
        }
    }

    public static <T> void logError(T error) {
        try {
            log.error(error.toString());
        } catch (NullPointerException ignored) {
            log.error("No error message provided");
        }
    }
}
