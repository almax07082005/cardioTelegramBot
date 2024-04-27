package com.example.cardiotelegrambot.config.enums;

import com.example.cardiotelegrambot.exceptions.NotCommandException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Commands {

    start("/start");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static Commands fromString(String name) throws NotCommandException {

        for (Commands command : Commands.values()) {
            if (command.name.equals(name)) {
                return command;
            }
        }

        throw new NotCommandException();
    }
}
