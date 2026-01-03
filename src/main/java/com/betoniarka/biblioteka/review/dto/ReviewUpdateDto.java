package com.betoniarka.biblioteka.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewUpdateDto(
        @Min(1) @Max(5) int rating,
        @NotBlank String comment
) { }
