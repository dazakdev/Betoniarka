package com.betoniarka.biblioteka.author.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthorCreateDto(@NotBlank String name) {}
