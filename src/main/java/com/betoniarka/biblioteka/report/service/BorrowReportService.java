package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.Book;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.report.dto.BorrowSummaryReportDto;
import com.betoniarka.biblioteka.report.dto.MostBorrowedBookDto;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BorrowReportService {

  private final AppUserRepository userRepository;
  private final BorrowRepository borrowRepository;

  public BorrowSummaryReportDto getSummary() {

    var borrowList = borrowRepository.findAll().stream().toList();

    long totalBorrows = borrowList.size();
    long currentBorrows = borrowList.stream().filter(borrow -> !borrow.isReturned()).count();

    long totalBorrowDurationDays =
        borrowList.stream()
            .filter(Borrow::isReturned)
            .mapToLong(
                borrow -> Duration.between(borrow.getBorrowedAt(), borrow.getReturnedAt()).toDays())
            .sum();

    long averageBorrowDurationDays = totalBorrowDurationDays / userRepository.count();

    long borrowsLastWeek =
        borrowList.stream()
            .filter(borrow -> Duration.between(borrow.getBorrowedAt(), Instant.now()).toDays() <= 7)
            .count();
    long borrowsLastMonth =
        borrowList.stream()
            .filter(
                borrow -> Duration.between(borrow.getBorrowedAt(), Instant.now()).toDays() <= 31)
            .count();
    long borrowsLastYear =
        borrowList.stream()
            .filter(
                borrow -> Duration.between(borrow.getBorrowedAt(), Instant.now()).toDays() <= 365)
            .count();

    return new BorrowSummaryReportDto(
        totalBorrows,
        currentBorrows,
        averageBorrowDurationDays,
        borrowsLastWeek,
        borrowsLastMonth,
        borrowsLastYear);
  }

  public List<MostBorrowedBookDto> getMostBorrowed(int limit, Instant from, Instant to) {

    boolean timePeriodNotSpecified = (from == null || to == null);

    var bookBorrowCountMap =
        borrowRepository.findAll().stream()
            .filter(
                borrow ->
                    timePeriodNotSpecified
                        || (borrow.getBorrowedAt().isAfter(from)
                            && borrow.getBorrowedAt().isBefore(to)))
            .map(Borrow::getBook)
            .collect(Collectors.groupingBy(book -> book, Collectors.counting()));

    return bookBorrowCountMap.entrySet().stream()
        .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
        .limit(limit)
        .map(
            entry ->
                new MostBorrowedBookDto(
                    entry.getKey().getId(), entry.getKey().getTitle(), entry.getValue()))
        .toList();
  }
}
