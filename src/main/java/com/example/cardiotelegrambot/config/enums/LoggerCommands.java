package com.example.cardiotelegrambot.config.enums;

import com.example.cardiotelegrambot.exceptions.NotCommandException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum LoggerCommands {

    start("/start"),
    getWinners("/getWinners");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static LoggerCommands fromString(String name) throws NotCommandException {

        for (LoggerCommands loggerCommand : LoggerCommands.values()) {
            if (loggerCommand.name.equals(name)) {
                return loggerCommand;
            }
        }

        throw new NotCommandException();
    }
}
