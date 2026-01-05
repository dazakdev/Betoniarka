package com.betoniarka.biblioteka.report.dto;

public record MostPopularBookCategoryDto(
        Long categoryId,
        String categoryName,
        long totalBooksBorrowed
) { }
