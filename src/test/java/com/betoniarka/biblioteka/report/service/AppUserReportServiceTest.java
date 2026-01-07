package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.report.dto.AppUserWithOverdueDto;
import com.betoniarka.biblioteka.report.dto.DeadAppUserAccountDto;
import com.betoniarka.biblioteka.report.dto.MostActiveAppUserDto;
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
        Mockito.when(borrowRepository.count()).thenReturn((long) borrowRepoContentMock.size());
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

    @Test
    void getDeadShouldNotIncludeUsersWithCurrentBorrows() {
        long thresholdDays = 30;
        List<DeadAppUserAccountDto> deadUsers = service.getDead(thresholdDays);

        assertThat(deadUsers)
                .allSatisfy(dto -> {
                    AppUser user = userRepository.findAll().stream()
                            .filter(u -> u.getId() == dto.userId())
                            .findFirst()
                            .orElseThrow();
                    assertThat(user.getCurrentBorrows()).isEmpty();
                });
    }

    @Test
    void getDeadShouldCalculateInActiveDaysCorrectly(@Autowired @Qualifier("clockReportMock") Clock clock) {
        long thresholdDays = 7;
        List<DeadAppUserAccountDto> deadUsers = service.getDead(thresholdDays);

        deadUsers.forEach(dto -> {
            AppUser user = userRepository.findAll().stream()
                    .filter(u -> u.getId() == dto.userId())
                    .findFirst()
                    .orElseThrow();
            Instant lastReturned = user.getBorrows().getLast().getReturnedAt();
            long expectedDays = Math.abs(Duration.between(lastReturned, Instant.now(clock)).toDays());
            assertThat(dto.inActiveDays()).isEqualTo(expectedDays);
        });
    }

    @Test
    void getDeadShouldReturnUsersInactiveLongerThanGivenDays(@Autowired @Qualifier("clockReportMock") Clock clock) {
        long thresholdDays = 30;

        List<DeadAppUserAccountDto> deadUsers = service.getDead(thresholdDays);

        assertThat(deadUsers).isNotEmpty();

        deadUsers.forEach(dto -> {
            AppUser user = userRepository.findAll().stream()
                    .filter(u -> u.getId() == dto.userId())
                    .findFirst()
                    .orElseThrow();

            assertThat(user.getCurrentBorrows()).isEmpty();

            Instant lastReturned = user.getBorrows().getLast().getReturnedAt();
            long inactiveDays = Duration.between(lastReturned, Instant.now(clock)).toDays();
            assertThat(inactiveDays).isGreaterThanOrEqualTo(thresholdDays);

            assertThat(dto.username()).isEqualTo(user.getUsername());
            assertThat(dto.inActiveDays()).isEqualTo(Math.abs(inactiveDays));
        });
    }

    @Test
    void getMostActiveShouldRespectLimit() {
        int limit = 3;
        assertThat(service.getMostActive(limit)).hasSizeLessThanOrEqualTo(limit);
    }

    @Test
    void getMostActiveShouldReturnEmptyListIfNoUsers() {
        Mockito.when(userRepository.findAll()).thenReturn(List.of());
        List<MostActiveAppUserDto> mostActive = service.getMostActive(5);
        assertThat(mostActive).isEmpty();
    }

    @Test
    void getMostActiveShouldReturnUsersSortedByBorrowsDescending() {
        int limit = 5;

        List<MostActiveAppUserDto> mostActive = service.getMostActive(limit);

        assertThat(mostActive).isNotEmpty();

        for (int i = 0; i < mostActive.size() - 1; i++) {
            assertThat(mostActive.get(i).totalBorrows())
                    .isGreaterThanOrEqualTo(mostActive.get(i + 1).totalBorrows());
        }

    }

}
