package com.betoniarka.biblioteka.appuser.dto;

import com.betoniarka.biblioteka.appuser.AppUserRole;
import jakarta.validation.constraints.Email;

public record AppUserAdminUpdateDto(
        String username,
        String firstname,
        String lastname,
        @Email String email,
        AppUserRole role,
        String password) {
}
