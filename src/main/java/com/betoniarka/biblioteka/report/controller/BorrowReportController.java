package com.betoniarka.biblioteka.report.controller;


import com.betoniarka.biblioteka.report.dto.BorrowSummaryReportDto;
import com.betoniarka.biblioteka.report.dto.MostBorrowedBookDto;
import com.betoniarka.biblioteka.report.service.BorrowReportService;
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
@RequestMapping("/report/borrow")
@RequiredArgsConstructor
public class BorrowReportController {

    private final BorrowReportService service;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<BorrowSummaryReportDto> summary() {
        return ResponseEntity.ok(service.getSummary());
    }

    @GetMapping("/most-borrowed")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<MostBorrowedBookDto>> mostBorrowed(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return ResponseEntity.ok(service.getMostBorrowed(limit, from, to));
    }

}
