package com.example.cardiotelegrambot.exceptions;

public class NoSuchUserException extends BaseException {

    @Override
    public String getMessage() {
        return "No such user exists";
    }
}
