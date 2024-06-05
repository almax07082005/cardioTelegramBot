package com.example.cardiotelegrambot.exceptions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class NotMemberException extends BaseException {

    private String username = "Current user";
    private Long chatId = 0L;

    @Override
    public String getMessage() {
        username = "\"" + username + "\"";
        return String.format(
                "%s_%s is not a member",
                username,
                chatId
        );
    }
}
