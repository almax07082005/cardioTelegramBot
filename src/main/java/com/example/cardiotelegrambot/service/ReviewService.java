package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.entity.ReviewEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchReviewException;
import com.example.cardiotelegrambot.exceptions.ReviewExistException;
import com.example.cardiotelegrambot.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

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
