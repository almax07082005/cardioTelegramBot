package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.entity.UserEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchUserException;
import com.example.cardiotelegrambot.exceptions.UserExistException;
import com.example.cardiotelegrambot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
