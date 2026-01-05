package com.betoniarka.biblioteka.report.dto;

public record AppUserSummaryReportDto(
        long totalAppUsers,
        long totalAppUsersWithBorrows,
        long totalAppUsersWithoutBorrows,
        long totalAppUsersWithOverdue,
        long averageBorrowsPerAppUser,
        long averageOverdueDaysPerAppUser
) { }
