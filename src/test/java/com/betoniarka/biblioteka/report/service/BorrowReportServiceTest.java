package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.Book;
import com.betoniarka.biblioteka.book.BookRepository;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.report.dto.BorrowSummaryReportDto;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(ReportServiceTestConfiguration.class)
@ExtendWith(MockitoExtension.class)
public class BorrowReportServiceTest {

    @Autowired
    @Qualifier("userRepoContentMock") List<AppUser> userRepoContentMock;
    @Autowired @Qualifier("borrowRepoContentMock") List<Borrow> borrowRepoContentMock;

    @MockitoBean AppUserRepository userRepository;
    @MockitoBean BorrowRepository borrowRepository;

    BorrowReportService service;

    @BeforeEach
    void setup() {
        Mockito.when(this.userRepository.findAll()).thenReturn(userRepoContentMock);
        Mockito.when(this.borrowRepository.findAll()).thenReturn(borrowRepoContentMock);
        Mockito.when(borrowRepository.count()).thenReturn((long) borrowRepoContentMock.size());
        Mockito.when(userRepository.count()).thenReturn((long) userRepoContentMock.size());

        this.service = new BorrowReportService(this.userRepository, this.borrowRepository);
    }

    @Test
    void getSummaryShouldReturnOverallBorrowStatistics() {
        BorrowSummaryReportDto summary = service.getSummary();

        assertThat(summary.totalBorrows()).isEqualTo(11);
        assertThat(summary.currentBorrows()).isEqualTo(5);
        assertThat(summary.borrowsLastWeek()).isEqualTo(3);
        assertThat(summary.borrowsLastMonth()).isEqualTo(4);
        assertThat(summary.borrowsLastYear()).isEqualTo(7);
    }

}
