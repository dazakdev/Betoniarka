package com.betoniarka.biblioteka.report.dto;

public record MostActiveAppUserDto(
        Long userId,
        String username,
        long totalBorrows
) { }
