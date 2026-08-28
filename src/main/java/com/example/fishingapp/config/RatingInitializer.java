package com.example.fishingapp.config;

import com.example.fishingapp.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RatingInitializer implements CommandLineRunner {

    @Autowired
    private RatingService ratingService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔄 Начинаю пересчёт рейтинга...");
        ratingService.recalculateAllUsersRating();
        System.out.println("✅ Рейтинг всех пользователей пересчитан");
    }
}