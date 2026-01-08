package com.betoniarka.biblioteka.author;

import com.betoniarka.biblioteka.author.dto.AuthorCreateDto;
import com.betoniarka.biblioteka.author.dto.AuthorResponseDto;
import com.betoniarka.biblioteka.author.dto.AuthorUpdateDto;
import com.betoniarka.biblioteka.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository repository;
    private final AuthorMapper mapper;

    public List<AuthorResponseDto> getAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public AuthorResponseDto getById(Long id) {
        return repository
                .findById(id)
                .map(mapper::toDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Author with id '%d' not found".formatted(id)));
    }

    public AuthorResponseDto create(AuthorCreateDto createDto) {
        var entityToSave = mapper.toEntity(createDto);
        var savedEntity = repository.save(entityToSave);
        return mapper.toDto(savedEntity);
    }

    public AuthorResponseDto update(Long id, AuthorUpdateDto updateDto) {
        var existingEntity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Author with id '%d' not found".formatted(id)));

        mapper.update(updateDto, existingEntity);
        var savedEntity = repository.save(existingEntity);
        return mapper.toDto(savedEntity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("Author with id '%d' not found".formatted(id));
        repository.deleteById(id);
    }
}
