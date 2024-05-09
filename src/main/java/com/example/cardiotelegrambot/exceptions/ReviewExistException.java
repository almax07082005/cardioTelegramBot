package com.example.cardiotelegrambot.exceptions;

public class ReviewExistException extends BaseException {

    @Override
    public String getMessage() {
        return "Review already exists";
    }
}
