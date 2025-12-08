package com.betoniarka.biblioteka.appuser;

public record UserResponseDto(

        long appUserId,

        String username,

        String firstname,

        String lastname,

        String email,

        String appRole
) { }
