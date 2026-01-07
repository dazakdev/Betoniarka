package com.betoniarka.biblioteka.report.dto;

public record AppUserWithOverdueDto(Long userId, String username, Long bookId, Long borrowId, long overdueDays) {}
