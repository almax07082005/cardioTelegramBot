package com.example.cardiotelegrambot.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {

    @Id
    private Long chatId;
    private String username;
    private Integer messageId;
    private Boolean isReferral;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Long> usersChatIds = new HashSet<>();
}
