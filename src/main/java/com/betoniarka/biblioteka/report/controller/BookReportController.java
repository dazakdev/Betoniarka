package com.betoniarka.biblioteka.report.controller;

import com.betoniarka.biblioteka.report.dto.*;
import com.betoniarka.biblioteka.report.service.BookReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/report/book")
@RequiredArgsConstructor
public class BookReportController {

    private final BookReportService bookReportService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<BookSummaryReportDto> summary() {
        return ResponseEntity.ok(bookReportService.getSummary());
    }

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookAvailabilityDto>> availability() {
        return ResponseEntity.ok(bookReportService.getAvailability());
    }

    @GetMapping("/most-borrowed")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<MostBorrowedBookDto>> mostBorrowed(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return ResponseEntity.ok(bookReportService.getMostBorrowed(limit, from, to));
    }

    @GetMapping("/most-reviewed")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<MostReviewedBookDto>> mostReviewed(
            @RequestParam(defaultValue = "10") int limit
    ) {
       return ResponseEntity.ok(bookReportService.getMostReviewed(limit));
    }

    @GetMapping("/most-popular-categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<MostPopularBookCategoryDto>> mostPopularBookCategories(
            @RequestParam(defaultValue = "10") int limit
    ) {
       return ResponseEntity.ok(bookReportService.getMostPopularCategories(limit));
    }

}
