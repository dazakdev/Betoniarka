package com.betoniarka.biblioteka.book;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query(
            """
            select distinct b
            from Book b
            left join fetch b.author
            left join fetch b.categories
            """)
    List<Book> findAllWithAuthorAndCategories();
}
