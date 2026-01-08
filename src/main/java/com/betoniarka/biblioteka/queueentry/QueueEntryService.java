package com.betoniarka.biblioteka.queueentry;

import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.BookRepository;
import com.betoniarka.biblioteka.borrow.Borrow;
import com.betoniarka.biblioteka.borrow.BorrowRepository;
import com.betoniarka.biblioteka.exceptions.ResourceConflictException;
import com.betoniarka.biblioteka.exceptions.ResourceNotFoundException;
import com.betoniarka.biblioteka.queueentry.dto.QueueEntryResponseDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class QueueEntryService {

    private final QueueEntryRepository queueEntryRepository;
    private final AppUserRepository appUserRepository;
    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final Clock clock;

    @Value("${library.queue.autoBorrowDurationDays:14}")
    private int autoBorrowDurationDays;

    public QueueEntryService(
            QueueEntryRepository queueEntryRepository,
            AppUserRepository appUserRepository,
            BookRepository bookRepository,
            BorrowRepository borrowRepository,
            Clock clock) {
        this.queueEntryRepository = queueEntryRepository;
        this.appUserRepository = appUserRepository;
        this.bookRepository = bookRepository;
        this.borrowRepository = borrowRepository;
        this.clock = clock;
    }

    public List<QueueEntryResponseDto> getQueueForBook(long bookId) {
        return queueEntryRepository.findByBookIdOrderByTimestampAsc(bookId).stream()
                .map(this::toDto)
                .toList();
    }

    public QueueEntryResponseDto joinQueue(String username, long bookId) {
        var user =
                appUserRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "AppUser with username '%s' not found".formatted(username)));

        var book =
                bookRepository
                        .findById(bookId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException("Book with id '%d' not found".formatted(bookId)));

        if (book.getCount() > 0) {
            throw new ResourceConflictException(
                    "Book '%s' is available; no need to queue".formatted(book.getTitle()));
        }

        if (queueEntryRepository.existsByAppUserIdAndBookId(user.getId(), book.getId())) {
            throw new ResourceConflictException(
                    "User '%s' is already queued for book '%s'".formatted(username, book.getTitle()));
        }

        QueueEntry entry = new QueueEntry();
        entry.setTimestamp(Instant.now(clock));
        entry.setAppUser(user);
        entry.setBook(book);

        user.getQueuedBooks().add(entry);
        book.getQueue().add(entry);

        var saved = queueEntryRepository.save(entry);
        return toDto(saved);
    }

    public void leaveQueue(String username, long bookId) {
        var user =
                appUserRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "AppUser with username '%s' not found".formatted(username)));

        QueueEntry entry =
                queueEntryRepository
                        .findByAppUserIdAndBookId(user.getId(), bookId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "QueueEntry for user '%s' and book '%d' not found"
                                                        .formatted(username, bookId)));

        entry.getBook().getQueue().remove(entry);
        entry.getAppUser().getQueuedBooks().remove(entry);
        queueEntryRepository.delete(entry);
    }

    public void tryAutoBorrowFromQueue(long bookId) {
        var book =
                bookRepository
                        .findById(bookId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException("Book with id '%d' not found".formatted(bookId)));
        tryAutoBorrowFromQueue(book);
    }

    public void tryAutoBorrowFromQueue(com.betoniarka.biblioteka.book.Book book) {
        while (book.getCount() > 0) {
            var nextOpt = queueEntryRepository.findFirstByBookIdOrderByTimestampAsc(book.getId());
            if (nextOpt.isEmpty()) return;

            QueueEntry entry = nextOpt.get();
            var user = entry.getAppUser();

            Borrow borrow = new Borrow();
            borrow.setBorrowDuration(Duration.ofDays(autoBorrowDurationDays));

            try {
                user.borrowBook(borrow, book);
                borrowRepository.save(borrow);
            } catch (ResourceConflictException ex) {
                // User can't borrow right now (limit/duplicate/etc.) — drop them from the queue.
            } finally {
                entry.getBook().getQueue().remove(entry);
                entry.getAppUser().getQueuedBooks().remove(entry);
                queueEntryRepository.delete(entry);
            }
        }
    }

    private QueueEntryResponseDto toDto(QueueEntry entry) {
        return new QueueEntryResponseDto(
                entry.getId(),
                entry.getTimestamp(),
                entry.getAppUser().getId(),
                entry.getAppUser().getUsername());
    }
}
