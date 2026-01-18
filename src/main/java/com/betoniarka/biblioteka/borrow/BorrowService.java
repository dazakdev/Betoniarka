package com.betoniarka.biblioteka.borrow;

import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.BookRepository;
import com.betoniarka.biblioteka.borrow.dto.BorrowCreateDto;
import com.betoniarka.biblioteka.borrow.dto.BorrowResponseDto;
import com.betoniarka.biblioteka.borrow.dto.BorrowUpdateDto;
import com.betoniarka.biblioteka.exceptions.ResourceNotFoundException;
import com.betoniarka.biblioteka.notifications.Notification;
import com.betoniarka.biblioteka.notifications.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BorrowService {

    private final AppUserRepository appUserRepository;
    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final NotificationRepository notificationRepository;
    private final BorrowMapper mapper;

    public List<BorrowResponseDto> getAll() {
        return borrowRepository.findAll().stream().map(mapper::toDto).toList();
    }

    public BorrowResponseDto getById(Long id) {
        return borrowRepository
                .findById(id)
                .map(mapper::toDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Borrow with id '%d' not found".formatted(id)));
    }

    public BorrowResponseDto borrowBook(BorrowCreateDto createBorrowDto) {
        var appUser =
                appUserRepository
                        .findById(createBorrowDto.appUserId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "AppUser with id '%d' not found".formatted(createBorrowDto.appUserId())));
        var book =
                bookRepository
                        .findById(createBorrowDto.bookId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Book with id '%d' not found".formatted(createBorrowDto.bookId())));

        var borrow = mapper.toEntity(createBorrowDto);

        appUser.borrowBook(borrow, book);

        borrowRepository.save(borrow);
        return mapper.toDto(borrow);
    }

    public BorrowResponseDto updateBorrow(Long id, BorrowUpdateDto updateBorrowDto) {
        var existingEntity =
                borrowRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Borrow with id '%d' not found".formatted(id)));

        mapper.update(updateBorrowDto, existingEntity);

        var savedEntity = borrowRepository.save(existingEntity);
        return mapper.toDto(savedEntity);
    }

    public BorrowResponseDto returnBook(Long id) {
        var borrow =
                borrowRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Borrow with id '%d' not found".formatted(id)));

        var appUser = borrow.getAppUser();
        appUser.returnBook(borrow);

        var book = borrow.getBook();
        var count = book.getCount();
        try {
            var notification = new Notification("Book '%s' is available for borrowing".formatted(book.getTitle()));
            var userToNotify = book.getQueue().get(count - 1).getAppUser();
            userToNotify.addNotification(notification);
            notification.setAppUser(userToNotify);
            notificationRepository.save(notification);
        } catch (Exception ignored) {
        }

        var savedBorrow = borrowRepository.save(borrow);
        return mapper.toDto(savedBorrow);
    }
}
