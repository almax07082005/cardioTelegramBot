package com.example.cardiotelegrambot.service.database;

import com.example.cardiotelegrambot.entity.ReviewEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchReviewException;
import com.example.cardiotelegrambot.exceptions.ReviewExistException;
import com.example.cardiotelegrambot.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public void createReview(ReviewEntity review) throws ReviewExistException {

        if (reviewRepository.getByChatId(review.getChatId()).isPresent()) {
            throw new ReviewExistException();
        }

        reviewRepository.save(review);
    }

    public ReviewEntity getReview(Long chatId) throws NoSuchReviewException {

        Optional<ReviewEntity> review = reviewRepository.getByChatId(chatId);
        if (review.isEmpty()) {
            throw new NoSuchReviewException();
        }

        return review.get();
    }

    public void deleteReview(Long chatId) throws NoSuchReviewException {

        Optional<ReviewEntity> review = reviewRepository.getByChatId(chatId);
        if (review.isEmpty()) {
            throw new NoSuchReviewException();
        }

        reviewRepository.delete(review.get());
    }
}
