package com.betoniarka.biblioteka.book;

import com.betoniarka.biblioteka.book.dto.BookCreateDto;
import com.betoniarka.biblioteka.book.dto.BookResponseDto;
import com.betoniarka.biblioteka.book.dto.BookUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(path = "books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping
    public List<BookResponseDto> getBooks(@RequestParam(required = false) String search) {
        return service.getAll(search);
    }

    @GetMapping("/{id}")
    public BookResponseDto getBookById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<BookResponseDto> createBook(@Valid @RequestBody BookCreateDto requestDto) {
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
    public BookResponseDto updateBook(
            @PathVariable Long id, @Valid @RequestBody BookUpdateDto requestDto) {
        return service.update(id, requestDto);
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        service.delete(id);
    }
}
