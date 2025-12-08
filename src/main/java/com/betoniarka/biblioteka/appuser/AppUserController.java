package com.betoniarka.biblioteka.appuser;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "appusers")
public class AppUserController {

    private final AppUserService service;

    public AppUserController(AppUserService service, PasswordEncoder passwordEncoder) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponseDto> getAppUsers() {
        return service.getAll().stream().map(service::toDto).toList();
    }

    @PostMapping
    public void createAppUser(@Valid @RequestBody UserCreateRequestDto requestDto) {
        service.create(requestDto);
    }

    @PutMapping
    public void updateAppUser(@Valid @RequestBody UserUpdateAppUserDto requestDto) {
        service.update(requestDto);
    }

    @DeleteMapping
    public void deleteAppUser(@Valid @RequestBody Long id) {
        service.delete(id);
    }

}
