package com.betoniarka.biblioteka.appuser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequestDto(

        @NotBlank String username,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password
) {}
