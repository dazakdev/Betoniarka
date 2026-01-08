package com.betoniarka.biblioteka.category;

import com.betoniarka.biblioteka.category.dto.CategoryCreateDto;
import com.betoniarka.biblioteka.category.dto.CategoryResponseDto;
import com.betoniarka.biblioteka.category.dto.CategoryUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public List<CategoryResponseDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<CategoryResponseDto> create(
            @Valid @RequestBody CategoryCreateDto requestDto) {
        var responseDto = service.create(requestDto);

        var location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(responseDto.id())
                        .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public CategoryResponseDto update(
            @PathVariable long id, @Valid @RequestBody CategoryUpdateDto requestDto) {
        return service.update(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
