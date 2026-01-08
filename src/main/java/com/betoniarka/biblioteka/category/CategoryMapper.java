package com.betoniarka.biblioteka.category;

import com.betoniarka.biblioteka.category.dto.CategoryCreateDto;
import com.betoniarka.biblioteka.category.dto.CategoryResponseDto;
import com.betoniarka.biblioteka.category.dto.CategoryUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "books", ignore = true)
    Category toEntity(CategoryCreateDto source);

    @Mapping(target = "books", ignore = true)
    void update(CategoryUpdateDto source, @MappingTarget Category target);

    CategoryResponseDto toDto(Category source);
}
