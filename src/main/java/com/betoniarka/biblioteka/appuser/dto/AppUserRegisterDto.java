package com.betoniarka.biblioteka.appuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AppUserRegisterDto(
    @NotBlank String username,
    @NotBlank String firstname,
    @NotBlank String lastname,
    @NotBlank @Email String email,
    @NotBlank String password) {}
