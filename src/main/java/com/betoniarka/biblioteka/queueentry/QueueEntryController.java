package com.betoniarka.biblioteka.queueentry;

import com.betoniarka.biblioteka.queueentry.dto.QueueEntryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("books/{bookId}/queue")
@RequiredArgsConstructor
public class QueueEntryController {

    private final QueueEntryService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public List<QueueEntryResponseDto> getQueue(@PathVariable long bookId) {
        return service.getQueueForBook(bookId);
    }

    @PostMapping("/join")
    @PreAuthorize("hasRole('APP_USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public QueueEntryResponseDto join(@PathVariable long bookId, Principal principal) {
        return service.joinQueue(principal.getName(), bookId);
    }

    @DeleteMapping("/leave")
    @PreAuthorize("hasRole('APP_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable long bookId, Principal principal) {
        service.leaveQueue(principal.getName(), bookId);
    }
}
