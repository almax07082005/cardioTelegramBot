package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.entity.UserEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchUserException;
import com.example.cardiotelegrambot.exceptions.UserExistException;
import com.example.cardiotelegrambot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(UserEntity user) throws UserExistException {

        if (userRepository.getByUsername(user.getUsername()).isPresent()) {
            throw new UserExistException();
        }

        userRepository.save(user);
    }

    public UserEntity getUser(String username) throws NoSuchUserException {

        Optional<UserEntity> user = userRepository.getByUsername(username);
        if (user.isEmpty()) {
            throw new NoSuchUserException();
        }

        return user.get();
    }

    public void updateUser(UserEntity user) throws NoSuchUserException {

        if (userRepository.getByUsername(user.getUsername()).isEmpty()) {
            throw new NoSuchUserException();
        }

        userRepository.save(user);
    }
}
