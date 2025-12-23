package com.betoniarka.biblioteka.review;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "review")
public class Review {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Setter
    @Column
    @NotNull(message = "rating is required")
    private int rating;

    @Getter
    @Setter
    @Column
    private String comment;

    @Getter
    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @Getter
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    public Review() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return id == review.id && rating == review.rating && Objects.equals(comment, review.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rating, comment);
    }

}
