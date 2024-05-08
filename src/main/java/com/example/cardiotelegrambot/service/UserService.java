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

    public UserEntity getByUsername(String username) throws NoSuchUserException {

        Optional<UserEntity> user = userRepository.getByUsername(username);
        if (user.isEmpty()) {
            throw new NoSuchUserException();
        }

        return user.get();
    }

    public void createUser(String username) throws UserExistException {

        if (userRepository.getByUsername(username).isPresent()) {
            throw new UserExistException();
        }

        userRepository.save(new UserEntity(username));
    }
}
