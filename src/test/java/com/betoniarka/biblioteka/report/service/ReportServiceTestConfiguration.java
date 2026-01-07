package com.betoniarka.biblioteka.report.service;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRole;
import com.betoniarka.biblioteka.book.Book;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.category.Category;
import com.betoniarka.biblioteka.review.Review;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@TestConfiguration
class ReportServiceTestConfiguration {

    @Bean
    @Qualifier("userRepoContentMock")
    public List<AppUser> userRepoContentMock() {
        return List.of(
                createUser(1L, "jdoe", "John", "Doe", "jdoe@example.com", AppUserRole.APP_USER),
                createUser(2L, "asmith", "Anna", "Smith", "asmith@example.com", AppUserRole.APP_USER),
                createUser(3L, "bwayne", "Bruce", "Wayne", "bwayne@example.com", AppUserRole.ADMIN),
                createUser(4L, "ckent", "Clark", "Kent", "ckent@example.com", AppUserRole.APP_USER),
                createUser(5L, "dprince", "Diana", "Prince", "dprince@example.com", AppUserRole.APP_USER),
                createUser(6L, "p.parker", "Peter", "Parker", "pparker@example.com", AppUserRole.APP_USER),
                createUser(7L, "tstark", "Tony", "Stark", "tstark@example.com", AppUserRole.ADMIN),
                createUser(8L, "srogers", "Steve", "Rogers", "srogers@example.com", AppUserRole.APP_USER)
        );
    }

    @Bean
    @Qualifier("categoryRepoContentMock")
    public List<Category> categoryContentMock() {
        return List.of(
                createCategory(1L, "Fantastyka"),
                createCategory(2L, "Kryminał"),
                createCategory(3L, "Science-Fiction"),
                createCategory(4L, "Dla dzieci"),
                createCategory(5L, "Dramat")
        );
    }

    @Bean
    @Qualifier("bookRepoContentMock")
    public List<Book> bookRepoContentMock(
        @Qualifier("categoryRepoContentMock") List<Category> categories
    ) {
        return List.of(
                createBook(1L, "Władca Pierścieni", 3, List.of(categories.get(0))),
                createBook(2L, "Harry Potter i Kamień Filozoficzny", 5, List.of(categories.get(0), categories.get(3))),
                createBook(3L, "Hobbit", 2, List.of(categories.get(0))),
                createBook(4L, "Gra o Tron", 4, List.of(categories.get(2))),
                createBook(5L, "Lalka", 1, List.of(categories.get(4))),
                createBook(6L, "Rok 1984", 3, List.of(categories.get(4))),
                createBook(7L, "Zbrodnia i kara", 2, List.of(categories.get(1))),
                createBook(8L, "Pieski małe dwa", 2, List.of(categories.get(3))),
                createBook(9L, "Koziołek Matołek", 0, List.of(categories.get(3)))
        );
    }

    @Bean
    @Qualifier("borrowRepoContentMock")
    public List<Borrow> borrowRepoContentMock(
            @Qualifier("userRepoContentMock") List<AppUser> users,
            @Qualifier("bookRepoContentMock") List<Book> books
    ) {

        List<Borrow> borrows = List.of(
                // 2023
                createBorrow(11L, users.get(2), books.get(3),
                        Instant.parse("2023-09-01T09:00:00Z"),
                        Duration.ofDays(20),
                        Instant.parse("2023-09-25T09:00:00Z")), // oddane w terminie
                createBorrow(12L, users.get(3), books.get(4),
                        Instant.parse("2023-12-15T08:00:00Z"),
                        Duration.ofDays(10),
                        null), // nadal aktywne, spóźnione

                // 2024
                createBorrow(9L, users.get(0), books.get(1),
                        Instant.parse("2024-06-15T10:00:00Z"),
                        Duration.ofDays(30),
                        Instant.parse("2024-07-20T12:00:00Z")), // oddane spóźnione
                createBorrow(10L, users.get(1), books.get(2),
                        Instant.parse("2024-12-20T08:00:00Z"),
                        Duration.ofDays(15),
                        Instant.parse("2025-01-10T10:00:00Z")), // oddane spóźnione

                // 2025 – oddane na czas
                createBorrow(1L, users.get(0), books.get(0),
                        Instant.parse("2025-12-01T10:00:00Z"),
                        Duration.ofDays(7),
                        Instant.parse("2025-12-07T09:00:00Z")), // oddane w terminie
                createBorrow(2L, users.get(1), books.get(1),
                        Instant.parse("2025-11-15T12:00:00Z"),
                        Duration.ofDays(10),
                        Instant.parse("2025-11-25T10:00:00Z")), // oddane w terminie

                // 2025-11 / 2025-12 – nadal aktywne, częściowo spóźnione
                createBorrow(8L, users.get(7), books.get(7),
                        Instant.parse("2025-11-30T15:00:00Z"),
                        Duration.ofDays(20),
                        null), // nadal aktywne, spóźnione
                createBorrow(7L, users.get(6), books.get(6),
                        Instant.parse("2025-12-25T09:00:00Z"),
                        Duration.ofDays(10),
                        null), // nadal aktywne, spóźnione

                // 2026 – nadal aktywne, w terminie
                createBorrow(5L, users.get(4), books.get(4),
                        Instant.parse("2026-01-03T10:00:00Z"),
                        Duration.ofDays(10),
                        null), // nadal aktywne, w terminie
                createBorrow(6L, users.get(5), books.get(5),
                        Instant.parse("2026-01-05T12:00:00Z"),
                        Duration.ofDays(7),
                        null), // nadal aktywne, w terminie

                // 2026 – oddane spóźnione
                createBorrow(4L, users.get(3), books.get(3),
                        Instant.parse("2026-01-01T08:00:00Z"),
                        Duration.ofDays(5),
                        Instant.parse("2026-01-10T08:00:00Z")) // oddane spóźnione
        );

        borrows.forEach(b -> {
            b.getAppUser().getBorrows().add(b);
            b.getBook().getBorrowedBy().add(b);
        });

        return borrows;
    }

    @Bean
    @Qualifier("clockReportMock")
    Clock clockReportMock() {
         return Clock.fixed(Instant.parse("2026-01-07T06:00:00Z"), ZoneOffset.UTC);
    }

    /*********************************************************************************************************************/

    private static AppUser createUser(long id, String username, String firstname, String lastname, String email, AppUserRole role) {
        AppUser u = new AppUser(id);
        u.setUsername(username);
        u.setFirstname(firstname);
        u.setLastname(lastname);
        u.setEmail(email);
        u.setPassword("password123");
        u.setRole(role);
        return u;
    }

    private static Category createCategory(long id, String name) {
        Category c = new Category(id);
        c.setName(name);
        return c;
    }

    private static Book createBook(long id, String title, int count, List<Category> categories) {
        Book b = new Book(id);
        b.setTitle(title);
        b.setCount(count);
        b.setCategories(categories);
        categories.forEach(category -> category.getBooks().add(b));
        return b;
    }

    private static Borrow createBorrow(long id, AppUser user, Book book, Instant borrowedAt, Duration borrowDuration, Instant returnedAt) {
        Borrow b = new Borrow(id);
        b.setAppUser(user);
        b.setBook(book);
        b.setBorrowedAt(borrowedAt);
        b.setBorrowDuration(borrowDuration);
        b.setReturnedAt(returnedAt);
        if (returnedAt == null) book.setCount(book.getCount() - 1);
        return b;
    }

}
