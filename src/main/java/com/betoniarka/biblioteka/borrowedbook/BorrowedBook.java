package com.betoniarka.biblioteka.borrowedbook;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.author.Author;
import com.betoniarka.biblioteka.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "borrowed_book")
public class BorrowedBook {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Setter
    @Column
    @NotNull(message = "timestamp is required")
    private Instant timestamp;

    @Getter
    @Setter
    @Column(name = "borrowed_time")
    @NotNull(message = "borrowedTime is required")
    private Duration borrowedTime;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    public BorrowedBook() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BorrowedBook that = (BorrowedBook) o;
        return id == that.id && timestamp == that.timestamp && borrowedTime == that.borrowedTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, borrowedTime);
    }

}
