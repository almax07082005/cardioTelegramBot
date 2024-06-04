package com.example.cardiotelegrambot.service.database;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.entity.UserEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchUserException;
import com.example.cardiotelegrambot.exceptions.UserExistException;
import com.example.cardiotelegrambot.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Logger logger;

    @Autowired
    public UserService(UserRepository userRepository, Logger logger) {
        this.userRepository = userRepository;
        this.logger = logger;
    }

    public void createUser(UserEntity user) throws UserExistException {

        if (userRepository.getByChatId(user.getChatId()).isPresent()) {
            throw new UserExistException();
        }

        userRepository.save(user);
    }

    public UserEntity getByChatId(Long chatId) throws NoSuchUserException {

        Optional<UserEntity> user = userRepository.getByChatId(chatId);
        if (user.isEmpty()) {
            throw new NoSuchUserException();
        }

        return user.get();
    }

    public void updateUser(UserEntity user) throws NoSuchUserException {

        if (userRepository.getByChatId(user.getChatId()).isEmpty()) {
            throw new NoSuchUserException();
        }

        userRepository.save(user);
    }

    public void storeUsersToCSV() {
        List<UserEntity> users = userRepository.findAll();

        try {
            FileWriter out = new FileWriter("users.csv");
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("chatId", "username", "usersAmount")
                    .build();
            try (CSVPrinter printer = new CSVPrinter(out, csvFormat)) {
                for (UserEntity user : users) {
                    printer.printRecord(user.getChatId(), user.getUsername(), user.getUsersChatIds().size());
                }
            }
        } catch (IOException exception) {
            logger.logException(exception);
        }
    }
}
