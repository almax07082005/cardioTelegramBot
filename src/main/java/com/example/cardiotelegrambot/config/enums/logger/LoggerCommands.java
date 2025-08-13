package com.example.cardiotelegrambot.config.enums.logger;

import com.example.cardiotelegrambot.exceptions.NotCommandException;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;

@AllArgsConstructor
public enum LoggerCommands {

    start("/start"),
    check("/check"),
    message("/message");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static Pair<LoggerCommands, Long> fromString(String name) throws NotCommandException {

        String[] commandsList = name.split(" ");
        if (commandsList[0].equals("/check") && commandsList.length == 2) {
            try {
                return Pair.of(LoggerCommands.check, Long.parseLong(commandsList[1]));
            } catch (NumberFormatException e) {
                return Pair.of(LoggerCommands.check, -1L);
            }
        }

        if (commandsList[0].equals("/message") && commandsList.length >= 2) {
            return Pair.of(LoggerCommands.message, -1L);
        }

        for (LoggerCommands command : LoggerCommands.values()) {
            if (command.name.equals(name)) {
                return Pair.of(command, -1L);
            }
        }

        throw new NotCommandException();
    }
}
