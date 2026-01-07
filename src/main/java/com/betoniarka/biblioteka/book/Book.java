package com.betoniarka.biblioteka.book;

import com.betoniarka.biblioteka.author.Author;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.category.Category;
import com.betoniarka.biblioteka.exceptions.ResourceConflictException;
import com.betoniarka.biblioteka.queueentry.QueueEntry;
import com.betoniarka.biblioteka.review.Review;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

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
  @Min(0)
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
  @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
  private List<Borrow> borrowedBy = new ArrayList<>();

  @Getter
  @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
  private List<QueueEntry> queue = new ArrayList<>();

  @Getter
  @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
  private List<Review> reviews = new ArrayList<>();

  public Book() {}

  public void addCategory(Category category) {
    if (this.categories.contains(category)) {
      return;
    }
    this.categories.add(category);
    category.getBooks().add(this);
  }

  public void removeCategory(Category category) {
    this.categories.remove(category);
    category.getBooks().remove(this);
  }

  public void setCategories(List<Category> newCategories) {
    var currentCategories = new ArrayList<>(this.categories);
    currentCategories.forEach(this::removeCategory);
    newCategories.forEach(this::addCategory);
  }

  public void decrementCount() {
    if (this.count <= 0) {
      throw new ResourceConflictException("Book '%s' is out of stock".formatted(this.title));
    }
    this.count--;
  }

  public void incrementCount() {
    this.count++;
  }

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
