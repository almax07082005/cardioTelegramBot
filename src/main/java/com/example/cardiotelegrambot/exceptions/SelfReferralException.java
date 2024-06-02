package com.example.cardiotelegrambot.exceptions;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SelfReferralException extends BaseException {

    private String username;
    private Long chatId;

    @Override
    public String getMessage() {
        return String.format(
                "User \"%s\"_%s tried to add himself by a referral link.",
                username,
                chatId
        );
    }
}
