package com.betoniarka.biblioteka.book;

import com.betoniarka.biblioteka.author.Author;
import com.betoniarka.biblioteka.borrowedbook.BorrowedBook;
import com.betoniarka.biblioteka.category.Category;
import com.betoniarka.biblioteka.queueentry.QueueEntry;
import com.betoniarka.biblioteka.review.Review;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "book")
public class Book {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Setter
    @Column(unique = true)
    @NotNull(message = "title is required")
    private String title;

    @Getter
    @Setter
    @Column
    @NotNull(message = "count is required")
    private int count;

    @Getter
    @ManyToMany
    @JoinTable(
        name = "book_category",
        joinColumns = @JoinColumn(name = "category_id", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "book_id", referencedColumnName = "ID"))
    private List<Category> categories = new ArrayList<>();

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @Getter
    @OneToMany(mappedBy = "book")
    private List<BorrowedBook> borrowedBy = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "book")
    private List<QueueEntry> queue = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "book")
    private List<Review> reviews = new ArrayList<>();

    public Book() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return id == book.id && count == book.count && Objects.equals(title, book.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, count);
    }

}
