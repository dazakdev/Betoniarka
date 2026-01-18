package com.betoniarka.biblioteka.appuser;

import com.betoniarka.biblioteka.appuser.dto.AppUserAdminUpdateDto;
import com.betoniarka.biblioteka.appuser.dto.AppUserCreateDto;
import com.betoniarka.biblioteka.appuser.dto.AppUserResponseDto;
import com.betoniarka.biblioteka.appuser.dto.AppUserUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(path = "appusers")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public List<AppUserResponseDto> getAppUsers() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public AppUserResponseDto getAppUserById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUserResponseDto> createAppUser(
            @Valid @RequestBody AppUserCreateDto requestDto) {
        var responseDto = service.create(requestDto);

        var location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(responseDto.id())
                        .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AppUserResponseDto updateAppUser(
            @PathVariable Long id, @Valid @RequestBody AppUserAdminUpdateDto requestDto) {
        return service.adminUpdate(id, requestDto);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAppUser(@PathVariable Long id) {
        service.deleteById(id);
    }

    @PatchMapping("/me")
    public AppUserResponseDto updateMe(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AppUserUpdateDto requestDto) {
        return service.nonAdminUpdate(principal.getUsername(), requestDto);
    }

    @DeleteMapping(path = "/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@AuthenticationPrincipal UserDetails principal) {
        service.deleteByUsername(principal.getUsername());
    }

    @GetMapping("/me")
    public AppUserResponseDto getMe(@AuthenticationPrincipal UserDetails principal) {
        return service.getByUsername(principal.getUsername());
    }
}
