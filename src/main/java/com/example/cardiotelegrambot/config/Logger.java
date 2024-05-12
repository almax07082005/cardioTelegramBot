package com.example.cardiotelegrambot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class Logger {

    private final LogConfig log;

    @Autowired
    public Logger(LogConfig log) {
        this.log = log;
    }

    public void logError(Exception exception) {
        log.error(exception.getMessage() +
                "\n" +
                String.join("\n", Arrays
                        .toString(exception.getStackTrace())
                        .split(" ")
        ));
    }

    public <T> void logWarn(T warn) {
        try {
            log.warn(warn.toString());
        } catch (NullPointerException ignored) {
            log.warn("No info message provided");
        }
    }

    public <T> void logInfo(T info) {
        try {
            log.info(info.toString());
        } catch (NullPointerException ignored) {
            log.info("No info message provided");
        }
    }

    public <T> void logError(T error) {
        try {
            log.error(error.toString());
        } catch (NullPointerException ignored) {
            log.error("No error message provided");
        }
    }
}
