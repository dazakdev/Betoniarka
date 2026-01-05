package com.betoniarka.biblioteka.report.dto;

public record BookSummaryReportDto(
        long totalCopies,
        long availableCopies,
        long currentlyBorrowedCopies,
        long totalCategories
) { }
