package com.betoniarka.biblioteka.review;

import com.betoniarka.biblioteka.appuser.AppUserMapper;
import com.betoniarka.biblioteka.book.BookMapper;
import com.betoniarka.biblioteka.review.dto.ReviewCreateDto;
import com.betoniarka.biblioteka.review.dto.ReviewResponseDto;
import com.betoniarka.biblioteka.review.dto.ReviewUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = { BookMapper.class, AppUserMapper.class }
)
public interface ReviewMapper {

    @Mapping(target = "book", ignore = true)
    @Mapping(target = "appUser", ignore = true)
    Review toEntity(ReviewCreateDto source);

    @Mapping(target = "book", ignore = true)
    @Mapping(target = "appUser", ignore = true)
    void update(ReviewUpdateDto source, @MappingTarget Review target);

    ReviewResponseDto toDto(Review source);

}
