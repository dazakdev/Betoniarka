package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.report.dto.AppUserSummaryReportDto;
import com.betoniarka.biblioteka.report.dto.AppUserWithOverdueDto;
import com.betoniarka.biblioteka.report.dto.DeadAppUserAccountDto;
import com.betoniarka.biblioteka.report.dto.MostActiveAppUserDto;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppUserReportService {

  private final AppUserRepository userRepository;
  private final BorrowRepository borrowRepository;
  private final Clock clock;

  public AppUserSummaryReportDto getSummary() {

    var appUsersList = userRepository.findAll().stream().toList();

    long totalAppUsers = appUsersList.size();

    long totalAppUsersWithBorrows =
        appUsersList.stream().filter(user -> !user.getCurrentBorrows().isEmpty()).count();

    long totalAppUsersWithoutBorrows = totalAppUsers - totalAppUsersWithBorrows;

    long totalAppUsersWithOverdue =
        appUsersList.stream()
            .filter(
                user -> {
                  for (Borrow borrow : user.getCurrentBorrows()) {
                    if (borrow
                        .getBorrowedAt()
                        .plus(borrow.getBorrowDuration())
                        .isBefore(Instant.now(clock))) return true;
                  }
                  return false;
                })
            .count();

    long totalBorrows = borrowRepository.count();
    long averageBorrowsPerAppUser = totalBorrows / totalAppUsers;

    long totalOverdueDays =
        borrowRepository.findAll().stream()
            .filter(Borrow::isReturned)
            .mapToLong(
                borrow -> {
                  Instant returnedTime = borrow.getReturnedAt();
                  Instant expectedReturnTime =
                      borrow.getBorrowedAt().plus(borrow.getBorrowDuration());
                  Duration duration = Duration.between(returnedTime, expectedReturnTime);
                  return duration.isNegative() ? 0 : duration.toDays();
                })
            .sum();

    long averageOverdueDaysPerAppUser = totalOverdueDays / totalAppUsers;

    long activeAppUsersLastWeek = totalAppUsers - getDead(7).size();
    double activityProportionLastWeek = ((double) activeAppUsersLastWeek / totalAppUsers);

    long activeAppUsersLastMonth = totalAppUsers - getDead(31).size();
    double activityProportionLastMonth = ((double) activeAppUsersLastMonth / totalAppUsers);

    return new AppUserSummaryReportDto(
        totalAppUsers,
        totalAppUsersWithBorrows,
        totalAppUsersWithoutBorrows,
        totalAppUsersWithOverdue,
        averageBorrowsPerAppUser,
        averageOverdueDaysPerAppUser,
        activeAppUsersLastWeek,
        activityProportionLastWeek,
        activeAppUsersLastMonth,
        activityProportionLastMonth);
  }

  public List<AppUserWithOverdueDto> getOverdue() {
    Instant now = Instant.now(clock);
    return borrowRepository.findAll().stream()
        .filter(
            borrow ->
                !borrow.isReturned()
                    && Duration.between(
                            now, borrow.getBorrowedAt().plus(borrow.getBorrowDuration()))
                        .isNegative())
        .map(
            borrow ->
                new AppUserWithOverdueDto(
                    borrow.getAppUser().getId(),
                    borrow.getAppUser().getUsername(),
                    borrow.getBook().getId(),
                    borrow.getId(),
                    Math.abs(Duration.between(
                            now, borrow.getBorrowedAt().plus(borrow.getBorrowDuration()))
                        .toDays())))
        .toList();
  }

  public List<MostActiveAppUserDto> getMostActive(int limit) {

    return userRepository.findAll().stream()
        .sorted(Comparator.comparingLong((AppUser user) -> user.getBorrows().size()).reversed())
        .limit(limit)
        .map(
            user ->
                new MostActiveAppUserDto(
                    user.getId(), user.getUsername(), user.getBorrows().size()))
        .toList();
  }

  public List<DeadAppUserAccountDto> getDead(long days) {

    return userRepository.findAll().stream()
        .filter(
            user ->
                user.getCurrentBorrows().isEmpty()
                    && Duration.between(user.getBorrows().getLast().getReturnedAt(), Instant.now(clock))
                            .toDays()
                        >= days)
        .map(
            user ->
                new DeadAppUserAccountDto(
                    user.getId(),
                    user.getUsername(),
                    Duration.between(user.getBorrows().getLast().getReturnedAt(), Instant.now(clock))
                        .toDays()))
        .toList();
  }
}
