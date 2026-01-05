package com.betoniarka.biblioteka.report.dto;

public record BorrowSummaryReportDto(
        long totalBorrows,
        long currentBorrows,
        long averageBorrowDurationDays
) { }
