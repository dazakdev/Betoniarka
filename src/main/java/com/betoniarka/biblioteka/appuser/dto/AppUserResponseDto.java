package com.betoniarka.biblioteka.appuser.dto;

import com.betoniarka.biblioteka.appuser.AppUserRole;

public record AppUserResponseDto(
        long id, String username, String firstname, String lastname, String email, AppUserRole role) {
}
