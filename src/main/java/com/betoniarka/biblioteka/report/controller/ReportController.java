package com.betoniarka.biblioteka.report.controller;

import com.betoniarka.biblioteka.report.service.AppUserReportService;
import com.betoniarka.biblioteka.report.service.BookReportService;
import com.betoniarka.biblioteka.report.service.BorrowReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final BookReportService bookReportService;
    private final AppUserReportService userReportService;
    private final BorrowReportService borrowReportService;

}
