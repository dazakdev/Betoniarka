package com.betoniarka.biblioteka.review.dto;

import com.betoniarka.biblioteka.appuser.dto.AppUserResponseDto;
import com.betoniarka.biblioteka.book.dto.BookResponseDto;

public record ReviewResponseDto(
        Long id,
        int rating,
        String comment,
        AppUserResponseDto appUser,
        BookResponseDto book
) { }
