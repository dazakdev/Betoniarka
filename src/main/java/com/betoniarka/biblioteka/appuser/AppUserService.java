package com.betoniarka.biblioteka.appuser;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> getAll() {
        return repository.findAll();
    }

    public Optional<UserResponseDto> create(UserCreateRequestDto requestDto) {
        AppUser user = new AppUser();
        user.setUsername(requestDto.username());
        user.setEmail(requestDto.email());
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        user.setRole(AppRole.APP_USER);

        AppUser saved = repository.save(user);
        return Optional.of(toDto(saved));
    }

    public Optional<UserResponseDto> update(UserUpdateAppUserDto requestDto) {
        Optional<AppUser> appUser = repository.findById(requestDto.id());
        appUser.ifPresent(appUser1 -> {
            appUser1.setUsername(requestDto.username());
            appUser1.setEmail(requestDto.email());
            appUser1.setPassword(passwordEncoder.encode(requestDto.password()));
            appUser1.setFirstname(requestDto.firstname());
            appUser1.setLastname(requestDto.lastname());
            repository.save(appUser1);
        });
        return appUser.map(this::toDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public UserResponseDto toDto(AppUser user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getRole().name()
        );
    }

}
