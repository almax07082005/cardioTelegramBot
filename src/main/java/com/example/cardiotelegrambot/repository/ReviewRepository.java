package com.example.cardiotelegrambot.repository;

import com.example.cardiotelegrambot.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Optional<ReviewEntity> getByChatId(Long chatId);
}
