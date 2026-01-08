package com.betoniarka.biblioteka.borrow.dto;

import java.time.Duration;

public record BorrowCreateDto(Duration borrowDuration, Long bookId, Long appUserId) {
}
