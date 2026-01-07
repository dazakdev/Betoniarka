package com.betoniarka.biblioteka.queueentry;

import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "queue_entry")
public class QueueEntry {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Getter
  @Setter
  @Column
  @NotNull(message = "borrowedAt is required")
  private Instant timestamp;

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

  public QueueEntry() {}

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    QueueEntry that = (QueueEntry) o;
    return id == that.id && Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, timestamp);
  }
}
