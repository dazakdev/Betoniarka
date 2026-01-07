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
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@SpringBootTest
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
            @Autowired @Qualifier("borrowRepoContentMock") List<Borrow> borrowRepoContentMock,
            @Autowired @Qualifier("clockReportMock") Clock clockReportMock
    ) {
        Mockito.when(this.userRepository.findAll()).thenReturn(userRepoContentMock);
        Mockito.when(this.borrowRepository.findAll()).thenReturn(borrowRepoContentMock);
        this.service = new AppUserReportService(this.userRepository, this.borrowRepository, clockReportMock);
    }

    @Test
    void shouldContainOverdueAppUsers() {
        assertThat(service.getOverdue()).isNotEmpty();
    }

    @Test
    void shouldContainCorrectNumberOfOverdue() {
        assertThat(service.getOverdue()).hasSize(3);
    }

    @Test
    void getOverdueShouldReturnAllAppUsersWithOverdue(@Autowired @Qualifier("clockReportMock") Clock clock) {
       List<AppUserWithOverdueDto> overdueList = service.getOverdue();
       List<Borrow> borrows = borrowRepository.findAll();

       overdueList.forEach(dto -> {
           Optional<Borrow> optionalBorrow = borrows.stream().filter(borrow -> dto.borrowId().equals(borrow.getId())).findAny();
           assertThat(optionalBorrow.isPresent()).isTrue();

           Borrow correspondingBorrow = optionalBorrow.get();
           assertThat(correspondingBorrow.isReturned()).isFalse();

           Instant dueDate = correspondingBorrow.getBorrowedAt().plus(correspondingBorrow.getBorrowDuration());
           long overdueDays = Math.abs(Duration.between(dueDate, Instant.now(clock)).toDays());
           assertThat(overdueDays).isEqualTo(dto.overdueDays());
       });
    }

}
