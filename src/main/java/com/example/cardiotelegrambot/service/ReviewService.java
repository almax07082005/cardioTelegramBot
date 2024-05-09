package com.example.cardiotelegrambot.service;

import com.example.cardiotelegrambot.entity.ReviewEntity;
import com.example.cardiotelegrambot.exceptions.NoSuchReviewException;
import com.example.cardiotelegrambot.exceptions.ReviewExistException;
import com.example.cardiotelegrambot.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public void createReview(ReviewEntity review) throws ReviewExistException {

        if (reviewRepository.getByUsername(review.getUsername()).isPresent()) {
            throw new ReviewExistException();
        }

        reviewRepository.save(review);
    }

    public ReviewEntity getReview(String username) throws NoSuchReviewException {

        Optional<ReviewEntity> review = reviewRepository.getByUsername(username);
        if (review.isEmpty()) {
            throw new NoSuchReviewException();
        }

        return review.get();
    }

    public void deleteReview(String username) throws NoSuchReviewException {

        Optional<ReviewEntity> review = reviewRepository.getByUsername(username);
        if (review.isEmpty()) {
            throw new NoSuchReviewException();
        }

        reviewRepository.delete(review.get());
    }
}
