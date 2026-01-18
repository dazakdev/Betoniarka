package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.Book;
import com.betoniarka.biblioteka.book.BookRepository;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.category.Category;
import com.betoniarka.biblioteka.report.dto.BookAvailabilityDto;
import com.betoniarka.biblioteka.report.dto.BookSummaryReportDto;
import com.betoniarka.biblioteka.report.dto.MostPopularBookCategoryDto;
import com.betoniarka.biblioteka.report.dto.MostReviewedBookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookReportService {

    private final AppUserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;

    public BookSummaryReportDto getSummary() {

        long available = bookRepository.findAll().stream().mapToLong(Book::getCount).sum();
        long borrowed =
                userRepository.findAll().stream().mapToLong(user -> user.getCurrentBorrows().size()).sum();
        long totalBooks = available + borrowed;

        double availabilityProportion = totalBooks == 0 ? 0 : ((double) available / totalBooks) * 100;
        double borrowProportion = totalBooks == 0 ? 0 : ((double) borrowed / totalBooks) * 100;

        long neverBorrowed =
                bookRepository.findAll().stream().filter(book -> book.getBorrowedBy().isEmpty()).count();

        long totalBorrows = borrowRepository.count();
        long borrowPerCopy = totalBooks == 0 ? 0 : totalBorrows / totalBooks;

        long totalCategories =
                bookRepository.findAll().stream().mapToLong(book -> book.getCategories().size()).sum();
        long categoriesPerBook = totalBooks == 0 ? 0 : totalCategories / totalBooks;

        return new BookSummaryReportDto(
                totalBooks,
                available,
                borrowed,
                availabilityProportion,
                borrowProportion,
                neverBorrowed,
                borrowPerCopy,
                totalCategories,
                categoriesPerBook);
    }

    public List<BookAvailabilityDto> getAvailability() {

        return bookRepository.findAll().stream()
                .filter(book -> book.getCount() > 0)
                .map(book -> new BookAvailabilityDto(book.getId(), book.getTitle(), book.getCount()))
                .toList();
    }

    public List<MostReviewedBookDto> getMostReviewed(int limit) {

        return bookRepository.findAll().stream()
                .sorted(Comparator.comparingInt((Book book) -> book.getReviews().size()).reversed())
                .limit(limit)
                .map(
                        book ->
                                new MostReviewedBookDto(book.getId(), book.getTitle(), book.getReviews().size()))
                .toList();
    }

    public List<MostPopularBookCategoryDto> getMostPopularCategories(int limit) {

        var categoryCountMap =
                borrowRepository.findAll().stream()
                        .flatMap(borrow -> borrow.getBook().getCategories().stream())
                        .collect(Collectors.groupingBy(category -> category, Collectors.counting()));

        return categoryCountMap.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue().reversed())
                .limit(limit)
                .map(
                        entry ->
                                new MostPopularBookCategoryDto(
                                        entry.getKey().getId(), entry.getKey().getName(), entry.getValue()))
                .toList();
    }
}
