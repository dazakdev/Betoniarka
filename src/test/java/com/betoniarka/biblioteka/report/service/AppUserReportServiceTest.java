package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.report.dto.AppUserSummaryReportDto;
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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(ReportServiceTestConfiguration.class)
@ExtendWith(MockitoExtension.class)
class AppUserReportServiceTest {

    @Autowired
    @Qualifier("userRepoContentMock")
    List<AppUser> userRepoContentMock;

    @Autowired
    @Qualifier("borrowRepoContentMock")
    List<Borrow> borrowRepoContentMock;

    @Autowired
    @Qualifier("clockReportMock")
    Clock clockReportMock;

    @MockitoBean
    AppUserRepository userRepository;
    @MockitoBean
    BorrowRepository borrowRepository;

    AppUserReportService service;

    @BeforeEach
    void setup() {
        Mockito.when(this.userRepository.findAll()).thenReturn(userRepoContentMock);
        Mockito.when(this.borrowRepository.findAll()).thenReturn(borrowRepoContentMock);
        Mockito.when(borrowRepository.count()).thenReturn((long) borrowRepoContentMock.size());

        this.service =
                new AppUserReportService(this.userRepository, this.borrowRepository, clockReportMock);
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
    void getOverdueShouldReturnAllAppUsersWithOverdue() {
        List<AppUserWithOverdueDto> overdueList = service.getOverdue();

        assertThat(overdueList)
                .extracting(AppUserWithOverdueDto::userId)
                .contains(4L, 7L, 8L)
                .doesNotContain(6L);

        assertThat(overdueList).allMatch(d -> d.overdueDays() > 0);
    }

    @Test
    void getDeadShouldReturnAllUsersWithLatestBorrowsOlderThanThreshold() {
        long thresholdDays = 30;
        List<DeadAppUserAccountDto> deadUsers = service.getDead(thresholdDays);
        assertThat(deadUsers)
                .extracting(DeadAppUserAccountDto::username)
                .containsExactlyInAnyOrder("bwayne", "asmith", "jdoe");
    }

    @Test
    void getDeadShouldCalculateInActiveDaysCorrectly(
            @Autowired @Qualifier("clockReportMock") Clock clock) {
        long thresholdDays = 7;
        List<DeadAppUserAccountDto> deadUsers = service.getDead(thresholdDays);

        deadUsers.forEach(
                dto -> {
                    AppUser user =
                            userRepository.findAll().stream()
                                    .filter(u -> u.getId() == dto.userId())
                                    .findFirst()
                                    .orElseThrow();
                    Instant lastReturned = user.getBorrows().getLast().getReturnedAt();
                    long expectedDays = Math.abs(Duration.between(lastReturned, Instant.now(clock)).toDays());
                    assertThat(dto.inActiveDays()).isEqualTo(expectedDays);
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

    @Test
    void getMostActiveShouldReturnTopUsersByBorrowCount() {
        int limit = 3;
        List<MostActiveAppUserDto> mostActive = service.getMostActive(limit);

        List<Long> expectedTopUsers = List.of(1L, 2L, 4L);
        assertThat(mostActive)
                .extracting(MostActiveAppUserDto::userId)
                .containsExactlyInAnyOrderElementsOf(expectedTopUsers);

        mostActive.forEach(dto -> assertThat(dto.totalBorrows()).isGreaterThan(0));
    }

    @Test
    void getSummaryShouldReflectDifferentUsers() {
        AppUserSummaryReportDto summary = service.getSummary();

        assertThat(summary.totalAppUsers()).isEqualTo(8);
        assertThat(summary.totalAppUsersWithBorrows()).isEqualTo(5);
        assertThat(summary.totalAppUsersWithoutBorrows()).isEqualTo(3);
        assertThat(summary.totalAppUsersWithOverdue()).isEqualTo(3);
        assertThat(summary.averageBorrowsPerAppUser()).isGreaterThan(0);
        assertThat(summary.activeAppUsersLastWeek()).isEqualTo(5);
        assertThat(summary.activeAppUsersLastMonth()).isEqualTo(6);
    }
}
