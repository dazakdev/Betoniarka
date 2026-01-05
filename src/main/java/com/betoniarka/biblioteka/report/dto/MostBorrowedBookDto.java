package com.betoniarka.biblioteka.report.dto;

public record MostBorrowedBookDto(
        Long bookId,
        String title,
        long totalBorrows
) { }
