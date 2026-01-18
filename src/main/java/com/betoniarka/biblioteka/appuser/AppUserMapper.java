package com.betoniarka.biblioteka.appuser;

import com.betoniarka.biblioteka.appuser.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppUserMapper {

    @Mapping(target = "borrows", ignore = true)
    @Mapping(target = "queuedBooks", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    AppUser toEntity(AppUserCreateDto source);

    @Mapping(target = "role", constant = "APP_USER")
    @Mapping(target = "borrows", ignore = true)
    @Mapping(target = "queuedBooks", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    AppUser toEntity(AppUserRegisterDto source);

    @Mapping(target = "password", source = "encryptedPassword")
    @Mapping(target = "borrows", ignore = true)
    @Mapping(target = "queuedBooks", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void update(
            AppUserAdminUpdateDto source, @MappingTarget AppUser target, String encryptedPassword);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "borrows", ignore = true)
    @Mapping(target = "queuedBooks", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void update(AppUserAdminUpdateDto source, @MappingTarget AppUser target);

    @Mapping(target = "password", source = "encryptedPassword")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "borrows", ignore = true)
    @Mapping(target = "queuedBooks", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void update(AppUserUpdateDto source, @MappingTarget AppUser target, String encryptedPassword);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "borrows", ignore = true)
    @Mapping(target = "queuedBooks", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void update(AppUserUpdateDto source, @MappingTarget AppUser target);

    AppUserResponseDto toDto(AppUser source);

    @Mapping(target = "role", constant = "APP_USER")
    AppUserAdminUpdateDto toAdminUpdateDto(AppUserUpdateDto source);

    @Mapping(target = "role", constant = "APP_USER")
    AppUserCreateDto toCreateDto(AppUserRegisterDto source);
}
