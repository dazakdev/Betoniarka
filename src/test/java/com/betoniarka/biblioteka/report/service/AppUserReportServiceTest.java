package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.Book;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.report.dto.AppUserWithOverdueDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(ReportServiceTestConfiguration.class)
@ExtendWith(MockitoExtension.class)
class AppUserReportServiceTest {

    @MockitoBean
    AppUserRepository userRepository;

    @MockitoBean
    BorrowRepository borrowRepository;

    AppUserReportService service;

    @BeforeEach
    void setup(
            @Autowired @Qualifier("userRepoContentMock") List<AppUser> userRepoContentMock,
            @Autowired @Qualifier("borrowRepoContentMock") List<Borrow> borrowRepoContentMock
    ) {
        Mockito.when(this.userRepository.findAll()).thenReturn(userRepoContentMock);
        Mockito.when(this.borrowRepository.findAll()).thenReturn(borrowRepoContentMock);

        // Ustalenie kontekstu czasowego dla testów
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-07T06:00:00Z"), ZoneOffset.UTC);
        this.service = new AppUserReportService(this.userRepository, this.borrowRepository, fixedClock);
    }


}
