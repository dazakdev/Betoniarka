package com.betoniarka.biblioteka.book.dto;

public record BookResponseDto(long id, String title, int count, Long authorId) {
}
