package com.betoniarka.biblioteka.report.dto;

public record MostReviewedBookDto(
        Long bookId,
        String title,
        long totalReviews
) { }
