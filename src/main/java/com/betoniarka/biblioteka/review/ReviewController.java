package com.betoniarka.biblioteka.review;

import com.betoniarka.biblioteka.review.dto.ReviewCreateDto;
import com.betoniarka.biblioteka.review.dto.ReviewResponseDto;
import com.betoniarka.biblioteka.review.dto.ReviewUpdateDto;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "review")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService service;

  @GetMapping
  public ResponseEntity<List<ReviewResponseDto>> getReviews() {

    return ResponseEntity.ok(service.getReviews());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long id) {

    return ResponseEntity.ok(service.getReviewById(id));
  }

  @PostMapping
  public ResponseEntity<ReviewResponseDto> createReview(
      @Valid @RequestBody ReviewCreateDto requestDto) {

    ReviewResponseDto responseDto = service.createReview(requestDto);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(responseDto.id())
            .toUri();

    return ResponseEntity.created(location).body(responseDto);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ReviewResponseDto> updateReview(
      @PathVariable Long id, @Valid @RequestBody ReviewUpdateDto requestDto) {

    return ResponseEntity.ok(service.updateReviewById(id, requestDto));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteReview(@PathVariable Long id) {

    service.deleteReviewById(id);
  }
}
