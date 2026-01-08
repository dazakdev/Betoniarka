package com.betoniarka.biblioteka.queueentry.dto;

import java.time.Instant;

public record QueueEntryResponseDto(long id, Instant timestamp, long appUserId, String username) {
}
