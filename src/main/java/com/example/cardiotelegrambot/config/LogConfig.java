package com.example.cardiotelegrambot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * This is a configuration class for logging.
 * It uses the Lombok library for logging and Spring's @Configuration annotation.
 * It provides methods for logging errors and information.
 */
@Configuration
@Slf4j
public abstract class LogConfig {

    /**
     * This method logs errors.
     * It takes an array of StackTraceElement objects as input and logs them as an error.
     * The stack trace elements are joined into a single string with newline characters between each element.
     *
     * @param stackTraceElements An array of StackTraceElement objects representing the stack trace of an error.
     */
    public static void logError(StackTraceElement[] stackTraceElements) {
        log.error(String.join("\n", Arrays.toString(stackTraceElements).split(" ")));
    }

    /**
     * This method logs information.
     * It takes an object of any type as input and logs its string representation as information.
     * If the object is null, a NullPointerException is caught and the stack trace of the exception is logged as an error.
     *
     * @param info An object of any type. Its string representation is logged as information.
     */
    public static <T> void logInfo(T info) {
        try {
            log.info(info.toString());
        } catch (NullPointerException exception) {
            LogConfig.logError(exception.getStackTrace());
        }
    }
}
