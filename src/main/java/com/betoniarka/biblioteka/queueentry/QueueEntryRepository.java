package com.betoniarka.biblioteka.queueentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    boolean existsByAppUserIdAndBookId(long appUserId, long bookId);

    Optional<QueueEntry> findByAppUserIdAndBookId(long appUserId, long bookId);

    Optional<QueueEntry> findFirstByBookIdOrderByTimestampAsc(long bookId);

    List<QueueEntry> findByBookIdOrderByTimestampAsc(long bookId);
}
