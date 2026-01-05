package com.betoniarka.biblioteka.report;

import com.betoniarka.biblioteka.report.dto.AppUserReportDto;
import com.betoniarka.biblioteka.report.dto.BookReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @GetMapping("/book")
    @PreAuthorize("hasRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<BookReportDto> getBookReport() {
        return ResponseEntity.ok(service.getBookReport());
    }

    @GetMapping("/appuser")
    @PreAuthorize("hasRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AppUserReportDto> getAppUserReport() {
        return ResponseEntity.ok(service.getAppUserReport());
    }

}
