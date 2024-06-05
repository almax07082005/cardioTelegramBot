package com.example.cardiotelegrambot.exceptions;

public class NotAdminException extends BaseException {

    @Override
    public String getMessage() {
        return "У Вас нет прав на использование данного бота.";
    }
}
