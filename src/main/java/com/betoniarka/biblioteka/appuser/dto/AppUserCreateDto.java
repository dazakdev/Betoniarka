package com.betoniarka.biblioteka.appuser.dto;

import com.betoniarka.biblioteka.appuser.AppUserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AppUserCreateDto(
    @NotBlank String username,
    String firstname,
    String lastname,
    @NotBlank @Email String email,
    AppUserRole role,
    @NotBlank String password) {

  public AppUserCreateDto {
    if (role == null) {
      role = AppUserRole.APP_USER;
    }
  }
}
