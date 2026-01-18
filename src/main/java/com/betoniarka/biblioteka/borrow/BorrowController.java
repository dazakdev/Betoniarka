package com.betoniarka.biblioteka.borrow;

import com.betoniarka.biblioteka.borrow.dto.BorrowCreateDto;
import com.betoniarka.biblioteka.borrow.dto.BorrowResponseDto;
import com.betoniarka.biblioteka.borrow.dto.BorrowUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(path = "borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public List<BorrowResponseDto> getBorrows() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public BorrowResponseDto getBorrowById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<BorrowResponseDto> createBorrow(
            @Valid @RequestBody BorrowCreateDto requestDto) {
        var responseDto = service.borrowBook(requestDto);

        var location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(responseDto.id())
                        .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public BorrowResponseDto updateBorrow(
            @PathVariable Long id, @Valid @RequestBody BorrowUpdateDto requestDto) {
        return service.updateBorrow(id, requestDto);
    }

    @PostMapping(path = "/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public BorrowResponseDto returnBook(@PathVariable Long id) {
        return service.returnBook(id);
    }
}
