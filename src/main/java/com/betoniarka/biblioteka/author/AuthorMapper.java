package com.betoniarka.biblioteka.author;

import com.betoniarka.biblioteka.author.dto.AuthorCreateDto;
import com.betoniarka.biblioteka.author.dto.AuthorResponseDto;
import com.betoniarka.biblioteka.author.dto.AuthorUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuthorMapper {

    Author toEntity(AuthorCreateDto source);

    void update(AuthorUpdateDto source, @MappingTarget Author target);

    AuthorResponseDto toDto(Author source);
}
