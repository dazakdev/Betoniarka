package com.betoniarka.biblioteka.borrow;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "borrowed_book")
public class Borrow {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Getter
  @Setter
  @Column(name = "borrowed_at")
  @NotNull(message = "borrowedAt is required")
  private Instant borrowedAt;

  @Getter
  @Setter
  @Column(name = "returned_at")
  private Instant returnedAt;

  @Getter
  @Setter
  @Column(name = "borrow_duration")
  @NotNull(message = "borrowDuration is required")
  private Duration borrowDuration;

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

  public Borrow() {}

    public Borrow(long id) {
      this.id = id;
    }

  public boolean isReturned() {
    return returnedAt != null;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Borrow that = (Borrow) o;
    return id == that.id && borrowedAt == that.borrowedAt && borrowDuration == that.borrowDuration;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, borrowedAt, borrowDuration);
  }
}
