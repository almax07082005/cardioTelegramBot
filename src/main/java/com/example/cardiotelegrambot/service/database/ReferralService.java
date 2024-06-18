package com.example.cardiotelegrambot.service.database;

import com.example.cardiotelegrambot.entity.ReferralEntity;
import com.example.cardiotelegrambot.repository.ReferralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;

    public Boolean isPresent() {
        return referralRepository.count() != 0;
    }

    public void startReferral() {
        if (isPresent()) {
            return;
        }
        referralRepository.save(ReferralEntity.builder()
                .isReferral(true)
                .build()
        );
    }

    public void finishReferral() {
        if (!isPresent()) {
            return;
        }
        referralRepository.delete(ReferralEntity.builder()
                .isReferral(true)
                .build()
        );
    }
}
