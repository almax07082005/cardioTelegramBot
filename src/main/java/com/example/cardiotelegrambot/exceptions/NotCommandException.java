package com.example.cardiotelegrambot.exceptions;

public class NotCommandException extends BaseException {

    @Override
    public String getMessage() {
        return "No such command to execute";
    }
}
