package com.betoniarka.biblioteka.borrow;

import com.betoniarka.biblioteka.appuser.AppUserMapper;
import com.betoniarka.biblioteka.book.BookMapper;
import com.betoniarka.biblioteka.borrow.dto.BorrowCreateDto;
import com.betoniarka.biblioteka.borrow.dto.BorrowResponseDto;
import com.betoniarka.biblioteka.borrow.dto.BorrowUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {BookMapper.class, AppUserMapper.class},
    imports = {java.time.Instant.class})
public interface BorrowMapper {

  @Mapping(target = "book", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "borrowedAt", ignore = true)
  @Mapping(target = "returnedAt", ignore = true)
  Borrow toEntity(BorrowCreateDto source);

  @Mapping(target = "book", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "borrowedAt", ignore = true)
  @Mapping(target = "returnedAt", ignore = true)
  void update(BorrowUpdateDto source, @MappingTarget Borrow target);

  BorrowResponseDto toDto(Borrow source);
}
