package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.Book;
import com.betoniarka.biblioteka.book.BookRepository;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.category.Category;
import com.betoniarka.biblioteka.category.CategoryRepository;
import com.betoniarka.biblioteka.report.dto.BookAvailabilityDto;
import com.betoniarka.biblioteka.report.dto.BookSummaryReportDto;
import com.betoniarka.biblioteka.report.dto.MostPopularBookCategoryDto;
import com.betoniarka.biblioteka.report.dto.MostReviewedBookDto;
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
public class BookReportServiceTest {

    @Autowired @Qualifier("userRepoContentMock") List<AppUser> userRepoContentMock;
    @Autowired @Qualifier("bookRepoContentMock") List<Book> bookRepoContentMock;
    @Autowired @Qualifier("borrowRepoContentMock") List<Borrow> borrowRepoContentMock;

    @MockitoBean AppUserRepository userRepository;
    @MockitoBean BookRepository bookRepository;
    @MockitoBean BorrowRepository borrowRepository;

    BookReportService service;

    @BeforeEach
    void setup() {
        Mockito.when(this.userRepository.findAll()).thenReturn(userRepoContentMock);
        Mockito.when(this.bookRepository.findAll()).thenReturn(bookRepoContentMock);
        Mockito.when(this.borrowRepository.findAll()).thenReturn(borrowRepoContentMock);
        Mockito.when(borrowRepository.count()).thenReturn((long) borrowRepoContentMock.size());

        this.service = new BookReportService(this.userRepository, this.bookRepository,  this.borrowRepository);
    }

    @Test
    void numberOfBooksInRepoShouldBeEqualToNine() {
        assertThat(bookRepository.findAll()).hasSize(9);
    }

    @Test
    void getAvailabilityShouldReturnBooksWithCountGreaterThanZero() {
        List<BookAvailabilityDto> availabilityList = service.getAvailability();
        assertThat(availabilityList)
                .hasSize(7)
                .extracting(BookAvailabilityDto::bookId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 6L, 7L, 8L);
    }

    @Test
    void getMostPopularCategoriesForLimitTwoShouldReturnFantasyAndForKids() {
        int threshold = 2;
        List<MostPopularBookCategoryDto> popularCategories = service.getMostPopularCategories(2);
        assertThat(popularCategories)
                .extracting(MostPopularBookCategoryDto::categoryName)
                .contains("Fantastyka");
        assertThat(popularCategories)
                .anyMatch(dto ->
                        dto.categoryName().equals("Dramat") ||
                        dto.categoryName().equals("Dla dzieci")
                );
    }

    @Test
    void getMostReviewedForLimitOneShouldReturnCrimeAndPunishment() {
        int threshold = 1;
        List<MostReviewedBookDto> mostReviewedBooks = service.getMostReviewed(1);
        assertThat(mostReviewedBooks)
                .extracting(MostReviewedBookDto::bookId)
                .containsExactly(7L);

        assertThat(mostReviewedBooks)
                .extracting(MostReviewedBookDto::totalReviews)
                .containsExactly((long) 2);
    }

    @Test
    void getSummaryShouldReturnOverallStatistics() {
        BookSummaryReportDto summary = service.getSummary();

        assertThat(summary.totalCopies()).isEqualTo(22);
        assertThat(summary.availableCopies()).isEqualTo( 17);
        assertThat(summary.currentlyBorrowedCopies()).isEqualTo(5);
        assertThat(summary.categoriesPerBook()).isEqualTo(0);
        assertThat(summary.borrowsPerCopy()).isEqualTo(0);
        assertThat(summary.neverBorrowedCopies()).isEqualTo(1);
    }

}
