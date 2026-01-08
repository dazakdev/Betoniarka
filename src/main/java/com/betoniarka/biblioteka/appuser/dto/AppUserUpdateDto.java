package com.betoniarka.biblioteka.appuser.dto;

import jakarta.validation.constraints.Email;

public record AppUserUpdateDto(
        String username, String firstname, String lastname, @Email String email, String password) {
}
