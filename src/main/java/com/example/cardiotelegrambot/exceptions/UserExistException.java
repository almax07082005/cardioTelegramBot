package com.example.cardiotelegrambot.exceptions;

public class UserExistException extends BaseException {

    @Override
    public String getMessage() {
        return "User already exists";
    }
}
