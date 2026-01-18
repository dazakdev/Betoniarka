package com.betoniarka.biblioteka.category;

import com.betoniarka.biblioteka.category.dto.CategoryCreateDto;
import com.betoniarka.biblioteka.category.dto.CategoryResponseDto;
import com.betoniarka.biblioteka.category.dto.CategoryUpdateDto;
import com.betoniarka.biblioteka.exceptions.ResourceConflictException;
import com.betoniarka.biblioteka.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public List<CategoryResponseDto> getAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public CategoryResponseDto getById(long id) {
        return repository
                .findById(id)
                .map(mapper::toDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category with id '%d' not found".formatted(id)));
    }

    public CategoryResponseDto create(CategoryCreateDto createDto) {
        if (repository.existsByName(createDto.name())) {
            throw new ResourceConflictException(
                    "Category with name '%s' already exists".formatted(createDto.name()));
        }

        var entityToSave = mapper.toEntity(createDto);
        var saved = repository.save(entityToSave);
        return mapper.toDto(saved);
    }

    public CategoryResponseDto update(long id, CategoryUpdateDto updateDto) {
        var existing =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException("Category with id '%d' not found".formatted(id)));

        if (updateDto.name() != null && repository.existsByNameAndIdNot(updateDto.name(), id)) {
            throw new ResourceConflictException(
                    "Category with name '%s' already exists".formatted(updateDto.name()));
        }

        mapper.update(updateDto, existing);
        var saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Category with id '%d' not found".formatted(id));
        }
        repository.deleteById(id);
    }
}
