package com.betoniarka.biblioteka.appuser;

public record UserUpdateAppUserDto(
        Long id,
        String username,
        String firstname,
        String lastname,
        String email,
        String password
) { }
