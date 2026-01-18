package com.betoniarka.biblioteka.category;

import com.betoniarka.biblioteka.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "category")
public class Category {

    @Getter
    @ManyToMany(mappedBy = "categories")
    private final List<Book> books = new ArrayList<>();
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Getter
    @Setter
    @Column(unique = true)
    @NotNull(message = "name is required")
    private String name;

    public Category() {
    }

    // Tests
    public Category(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id && Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
