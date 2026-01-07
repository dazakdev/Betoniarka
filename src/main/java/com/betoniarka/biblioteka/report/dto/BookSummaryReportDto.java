package com.betoniarka.biblioteka.report.dto;

public record BookSummaryReportDto(
    long totalCopies,
    long availableCopies,
    long currentlyBorrowedCopies,
    double availabilityProportion,
    double borrowProportion,
    long neverBorrowedCopies,
    long borrowsPerCopy,
    long totalCategories,
    long categoriesPerBook) {}
