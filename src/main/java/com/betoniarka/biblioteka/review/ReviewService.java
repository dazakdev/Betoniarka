package com.betoniarka.biblioteka.review;


import com.betoniarka.biblioteka.appuser.AppUser;
import com.betoniarka.biblioteka.appuser.AppUserRepository;
import com.betoniarka.biblioteka.book.Book;
import com.betoniarka.biblioteka.book.BookRepository;
import com.betoniarka.biblioteka.exceptions.ResourceConflictException;
import com.betoniarka.biblioteka.exceptions.ResourceNotFoundException;
import com.betoniarka.biblioteka.review.dto.ReviewCreateDto;
import com.betoniarka.biblioteka.review.dto.ReviewResponseDto;
import com.betoniarka.biblioteka.review.dto.ReviewUpdateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final AppUserRepository appUserRepo;
    private final BookRepository bookRepo;
    private final ReviewMapper mapper;

    /**
     * Retrieves all reviews available in the system.
     *
     * @return list of all reviews mapped to {@link ReviewResponseDto}
     */
    public List<ReviewResponseDto> getReviews() {

        return reviewRepo.findAll().stream().map(mapper::toDto).toList();

    }

    /**
     * Retrieves a single review by its identifier.
     *
     * @param id identifier of the review
     * @return review mapped to {@link ReviewResponseDto}
     * @throws ResourceNotFoundException if no review with the given id exists
     */
    public ReviewResponseDto getReviewById(Long id) throws ResourceNotFoundException {

        return reviewRepo.findById(id).map(mapper::toDto).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Review with id '%d' not found".formatted(id)
                )
        );

    }

    /**
     * Creates a new review for a given book by a specific user.
     *
     * @param dto data required to create a review
     * @return created review mapped to {@link ReviewResponseDto}
     *
     * @throws ResourceNotFoundException if the user or the book does not exist
     * @throws ResourceConflictException if the user has already reviewed the book
     */
    public ReviewResponseDto createReview(ReviewCreateDto dto)
            throws ResourceNotFoundException, ResourceConflictException {

        AppUser appUser = appUserRepo.findById(dto.appUserId()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Appuser with id '%d' not found".formatted(dto.appUserId())
                )
        );

        Book book = bookRepo.findById(dto.bookId()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Book with id '%d' not found".formatted(dto.bookId())
                )
        );

        Review review = mapper.toEntity(dto);
        appUser.addReview(review, book);

        reviewRepo.save(review);
        return mapper.toDto(review);

    }

    /**
     * Updates an existing review with new data.
     *
     * @param id identifier of the review to update
     * @param dto data used to update the review
     * @return updated review mapped to {@link ReviewResponseDto}
     *
     * @throws ResourceNotFoundException if the review with the given id does not exist
     */
    public ReviewResponseDto updateReviewById(Long id, ReviewUpdateDto dto) throws ResourceNotFoundException {

        Review existingReview = reviewRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Review with id '%d' not found"
                )
        );

        mapper.update(dto, existingReview);
        Review savedReview = reviewRepo.save(existingReview);
        return mapper.toDto(savedReview);

    }

    /**
     * Deletes a review by its identifier.
     *
     * <p>The review is removed both from the database and
     * from the user's review collection to keep the relationship consistent.</p>
     *
     * @param id identifier of the review to delete
     *
     * @throws ResourceNotFoundException if the review with the given id does not exist
     * @throws IllegalStateException if the review cannot be removed from the user
     */
    public void deleteReviewById(Long id)
            throws ResourceNotFoundException, IllegalStateException {

        Review review = reviewRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Review with id '%d' not found")
        );

        AppUser user = review.getAppUser();
        user.deleteReview(review);
        reviewRepo.delete(review);

    }

}
