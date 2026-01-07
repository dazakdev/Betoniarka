package com.betoniarka.biblioteka.book;

import com.betoniarka.biblioteka.book.dto.BookCreateDto;
import com.betoniarka.biblioteka.book.dto.BookResponseDto;
import com.betoniarka.biblioteka.book.dto.BookUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {

  @Mapping(target = "categories", ignore = true)
  @Mapping(target = "author", ignore = true)
  @Mapping(target = "borrowedBy", ignore = true)
  @Mapping(target = "queue", ignore = true)
  @Mapping(target = "reviews", ignore = true)
  Book toEntity(BookCreateDto source);

  @Mapping(target = "categories", ignore = true)
  @Mapping(target = "author", ignore = true)
  @Mapping(target = "borrowedBy", ignore = true)
  @Mapping(target = "queue", ignore = true)
  @Mapping(target = "reviews", ignore = true)
  void update(BookUpdateDto source, @MappingTarget Book target);

  @Mapping(
      target = "authorId",
      expression = "java(source.getAuthor() != null ? source.getAuthor().getId() : null)")
  BookResponseDto toDto(Book source);
}
