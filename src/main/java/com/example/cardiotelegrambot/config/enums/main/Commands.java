package com.example.cardiotelegrambot.config.enums.main;

import com.example.cardiotelegrambot.exceptions.NotCommandException;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;

@AllArgsConstructor
public enum Commands {

    start("/start");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static Pair<Commands, String> fromString(String name) throws NotCommandException {
        if (name == null) {
            throw new NotCommandException();
        }

        String[] commandsList = name.split(" ");
        if (commandsList[0].equals("/start") && commandsList.length == 2) {
            return Pair.of(Commands.start, commandsList[1]);
        }

        for (Commands command : Commands.values()) {
            if (command.name.equals(name)) {
                return Pair.of(command, "");
            }
        }

        throw new NotCommandException();
    }
}
