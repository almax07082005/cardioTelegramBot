package com.example.cardiotelegrambot.exceptions;

public class NotMemberException extends BaseException {

    @Override
    public String getMessage() {
        return "Current user is not a member";
    }
}
