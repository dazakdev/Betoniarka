package com.betoniarka.biblioteka.borrow.dto;

import com.betoniarka.biblioteka.appuser.dto.AppUserResponseDto;
import com.betoniarka.biblioteka.book.dto.BookResponseDto;
import java.time.Duration;
import java.time.Instant;

public record BorrowResponseDto(
    long id,
    Instant borrowedAt,
    Instant returnedAt,
    Duration borrowDuration,
    BookResponseDto book,
    AppUserResponseDto appUser) {}
