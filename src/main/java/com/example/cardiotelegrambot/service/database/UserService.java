package com.example.cardiotelegrambot.service.database;

import com.example.cardiotelegrambot.config.Logger;
import com.example.cardiotelegrambot.entity.UserEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchUserException;
import com.example.cardiotelegrambot.exceptions.NotMemberException;
import com.example.cardiotelegrambot.exceptions.UserExistException;
import com.example.cardiotelegrambot.repository.UserRepository;
import com.example.cardiotelegrambot.service.bot.main.Button;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Logger logger;
    private final Button button;

    @Value("${spring.data.table}")
    private String tableFilename;

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

    @Builder(toBuilder = true)
    private static class UserDTO {
        private Long chatId;
        private String username;
        private Integer usersAmount;
    }

    private List<UserDTO> getAllFiltered() {
        List<UserEntity> allUsers = userRepository.findAll();
        List<UserDTO> filteredUsers = new ArrayList<>();

        for (UserEntity user : allUsers) {
            int usersAmount = 0;

            for (Long chatId : user.getUsersChatIds()) {
                boolean isSubscribed;
                try {
                    button
                            .setByVariables(chatId)
                            .isSubscribed();
                    isSubscribed = true;
                } catch (NotMemberException exception) {
                    isSubscribed = false;
                }

                if (isSubscribed) {
                    usersAmount++;
                }
            }

            filteredUsers.add(UserDTO.builder()
                    .chatId(user.getChatId())
                    .username(user.getUsername())
                    .usersAmount(usersAmount)
                    .build()
            );
        }

        return filteredUsers;
    }

    public void storeUsersToCSV() {
        List<UserDTO> users = getAllFiltered();

        try {
            FileWriter out = new FileWriter(tableFilename);
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("chatId", "username", "usersAmount")
                    .build();
            try (CSVPrinter printer = new CSVPrinter(out, csvFormat)) {
                for (UserDTO user : users) {
                    printer.printRecord(user.chatId, user.username, user.usersAmount);
                }
            }
        } catch (IOException exception) {
            logger.logException(exception);
        }
    }
}
