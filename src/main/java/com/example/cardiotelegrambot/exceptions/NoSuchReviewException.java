package com.example.cardiotelegrambot.exceptions;

public class NoSuchReviewException extends BaseException {

    @Override
    public String getMessage() {
        return "No such review exists";
    }
}
