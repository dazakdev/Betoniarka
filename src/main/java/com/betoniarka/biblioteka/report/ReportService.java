package com.betoniarka.biblioteka.report;

import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.BookRepository;
import com.betoniarka.biblioteka.report.dto.AppUserReportDto;
import com.betoniarka.biblioteka.report.dto.BookReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppUserRepository userRepository;
    private final BookRepository bookRepository;

    public BookReportDto getBookReport() {
        // TODO
        return null;
    }

    public AppUserReportDto getAppUserReport() {
        // TODO
        return null;
    }

}
