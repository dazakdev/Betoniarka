package com.betoniarka.biblioteka.queueentry;

import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.BookRepository;
import com.betoniarka.biblioteka.exceptions.ResourceNotFoundException;
import com.betoniarka.biblioteka.queueentry.dto.QueueEntryResponseDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class QueueEntryService {

    private final QueueEntryRepository queueEntryRepository;
    private final AppUserRepository appUserRepository;
    private final BookRepository bookRepository;

    @Value("${library.queue.autoBorrowDurationDays:14}")
    private int autoBorrowDurationDays;

    public QueueEntryService(
            QueueEntryRepository queueEntryRepository,
            AppUserRepository appUserRepository,
            BookRepository bookRepository) {
        this.queueEntryRepository = queueEntryRepository;
        this.appUserRepository = appUserRepository;
        this.bookRepository = bookRepository;
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

        var queueEntry = new QueueEntry();
        user.joinQueue(queueEntry, book);

        var saved = queueEntryRepository.save(queueEntry);
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

        var entry =
                queueEntryRepository
                        .findByAppUserIdAndBookId(user.getId(), bookId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "QueueEntry for user '%s' and book '%d' not found"
                                                        .formatted(username, bookId)));

        user.leaveQueue(entry);
        queueEntryRepository.delete(entry);
    }

    private QueueEntryResponseDto toDto(QueueEntry entry) {
        return new QueueEntryResponseDto(
                entry.getId(),
                entry.getTimestamp(),
                entry.getAppUser().getId(),
                entry.getAppUser().getUsername());
    }
}
