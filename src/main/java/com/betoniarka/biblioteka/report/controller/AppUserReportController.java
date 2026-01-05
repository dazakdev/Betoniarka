package com.betoniarka.biblioteka.report.controller;

import com.betoniarka.biblioteka.report.dto.AppUserSummaryReportDto;
import com.betoniarka.biblioteka.report.dto.AppUserWithOverdueDto;
import com.betoniarka.biblioteka.report.dto.DeadAppUserAccountDto;
import com.betoniarka.biblioteka.report.dto.MostActiveAppUserDto;
import com.betoniarka.biblioteka.report.service.AppUserReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report/appuser")
@RequiredArgsConstructor
public class AppUserReportController {

    private final AppUserReportService service;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AppUserSummaryReportDto> summary() {
        return ResponseEntity.ok(service.getSummary());
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<AppUserWithOverdueDto>> overdue() {
        return ResponseEntity.ok(service.getOverdue());
    }

    @GetMapping("/most-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<MostActiveAppUserDto>> mostActive(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(service.getMostActive(limit));
    }

    @GetMapping("/dead")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<DeadAppUserAccountDto>> dead(
            @RequestParam(defaultValue = "100") int days
    ) {
        return ResponseEntity.ok(service.getDead(days));
    }

}
