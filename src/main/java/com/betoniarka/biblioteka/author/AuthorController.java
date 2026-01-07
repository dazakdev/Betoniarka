package com.betoniarka.biblioteka.author;

import com.betoniarka.biblioteka.author.dto.AuthorCreateDto;
import com.betoniarka.biblioteka.author.dto.AuthorResponseDto;
import com.betoniarka.biblioteka.author.dto.AuthorUpdateDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "authors")
@RequiredArgsConstructor
public class AuthorController {

  private final AuthorService service;

  @GetMapping
  public List<AuthorResponseDto> getAuthors() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  public AuthorResponseDto getAuthorById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  public ResponseEntity<AuthorResponseDto> createAuthor(
      @Valid @RequestBody AuthorCreateDto requestDto) {
    var responseDto = service.create(requestDto);

    var location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(responseDto.id())
            .toUri();

    return ResponseEntity.created(location).body(responseDto);
  }

  @PatchMapping(path = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  public AuthorResponseDto updateAuthor(
      @PathVariable Long id, @Valid @RequestBody AuthorUpdateDto requestDto) {
    return service.update(id, requestDto);
  }

  @DeleteMapping(path = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAuthor(@PathVariable Long id) {
    service.delete(id);
  }
}
