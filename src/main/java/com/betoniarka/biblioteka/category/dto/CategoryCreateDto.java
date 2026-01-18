package com.betoniarka.biblioteka.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateDto(@NotBlank String name) {
}
