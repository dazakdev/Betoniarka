package com.betoniarka.biblioteka.report.controller;

import com.betoniarka.biblioteka.report.dto.BookAvailabilityDto;
import com.betoniarka.biblioteka.report.dto.BookSummaryReportDto;
import com.betoniarka.biblioteka.report.dto.MostPopularBookCategoryDto;
import com.betoniarka.biblioteka.report.dto.MostReviewedBookDto;
import com.betoniarka.biblioteka.report.service.BookReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report/book")
@RequiredArgsConstructor
public class BookReportController {

    private final BookReportService service;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<BookSummaryReportDto> summary() {
        return ResponseEntity.ok(service.getSummary());
    }

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookAvailabilityDto>> availability() {
        return ResponseEntity.ok(service.getAvailability());
    }

    @GetMapping("/most-reviewed")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<MostReviewedBookDto>> mostReviewed(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getMostReviewed(limit));
    }

    @GetMapping("/most-popular-categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<MostPopularBookCategoryDto>> mostPopularBookCategories(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getMostPopularCategories(limit));
    }
}
