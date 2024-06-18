package com.example.cardiotelegrambot.repository;

import com.example.cardiotelegrambot.entity.ReferralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralRepository extends JpaRepository<ReferralEntity, Boolean> {}
