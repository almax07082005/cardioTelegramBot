package com.example.cardiotelegrambot.exceptions;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AlreadyReferralException extends BaseException {

    private String username;
    private Long chatId;

    @Override
    public String getMessage() {
        return String.format(
                "User \"%s\"_%s has already used referral program.",
                username,
                chatId
        );
    }
}
