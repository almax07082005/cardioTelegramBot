package com.example.cardiotelegrambot.exceptions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class NotMemberException extends BaseException {

    private String username = "Current user";

    @Override
    public String getMessage() {
        return (username.equals("Current user") ? "" : "@") + username + " is not a member";
    }
}
