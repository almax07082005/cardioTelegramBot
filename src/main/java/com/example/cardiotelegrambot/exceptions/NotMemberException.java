package com.example.cardiotelegrambot.exceptions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class NotMemberException extends BaseException {

    private String username = "Current user";

    @Override
    public String getMessage() {
        username = "\"" + username + "\"";
        return String.format(
                "%s is not a member",
                username
        );
    }
}
