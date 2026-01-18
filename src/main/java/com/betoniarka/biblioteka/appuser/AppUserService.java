package com.betoniarka.biblioteka.appuser;

import com.betoniarka.biblioteka.appuser.dto.*;
import com.betoniarka.biblioteka.exceptions.ResourceConflictException;
import com.betoniarka.biblioteka.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository repository;
    private final AppUserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public List<AppUserResponseDto> getAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public AppUserResponseDto getById(Long id) {
        return repository
                .findById(id)
                .map(mapper::toDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException("AppUser with id '%d' not found".formatted(id)));
    }

    public AppUserResponseDto create(AppUserCreateDto createDto) {
        if (repository.existsByUsername(createDto.username()))
            throw new ResourceConflictException(
                    "AppUser with username '%s' already exists".formatted(createDto.username()));

        if (repository.existsByEmail(createDto.email()))
            throw new ResourceConflictException(
                    "AppUser with email '%s' already exists".formatted(createDto.email()));

        var entityToSave = mapper.toEntity(createDto);
        entityToSave.setPassword(passwordEncoder.encode(createDto.password()));
        var savedEntity = repository.save(entityToSave);
        return mapper.toDto(savedEntity);
    }

    public AppUserResponseDto adminUpdate(Long id, AppUserAdminUpdateDto updateDto) {
        if (updateDto.username() != null
                && repository.existsByUsernameAndIdNot(updateDto.username(), id))
            throw new ResourceConflictException(
                    "AppUser with username '%s' already exists".formatted(updateDto.username()));

        if (updateDto.email() != null && repository.existsByEmailAndIdNot(updateDto.email(), id))
            throw new ResourceConflictException(
                    "AppUser with email '%s' already exists".formatted(updateDto.email()));

        var existingEntity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException("AppUser with id '%d' not found".formatted(id)));

        mapper.update(updateDto, existingEntity, passwordEncoder.encode(updateDto.password()));
        var savedEntity = repository.save(existingEntity);
        return mapper.toDto(savedEntity);
    }

    public void deleteById(Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("AppUser with id '%d' not found".formatted(id));
        repository.deleteById(id);
    }

    public AppUserResponseDto register(AppUserRegisterDto registerDto) {
        return create(mapper.toCreateDto(registerDto));
    }

    public AppUserResponseDto nonAdminUpdate(String username, AppUserUpdateDto updateDto) {
        var existingEntity =
                repository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "AppUser with username '%s' not found".formatted(username)));
        return adminUpdate(existingEntity.getId(), mapper.toAdminUpdateDto(updateDto));
    }

    public AppUserResponseDto getByUsername(String username) {
        return repository
                .findByUsername(username)
                .map(mapper::toDto)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "AppUser with username '%s' not found".formatted(username)));
    }

    public void deleteByUsername(String username) {
        deleteById(
                repository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "AppUser with username '%s' not found".formatted(username)))
                        .getId());
    }
}
