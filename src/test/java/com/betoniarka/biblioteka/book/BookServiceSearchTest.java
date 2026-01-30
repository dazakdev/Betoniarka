package com.betoniarka.biblioteka.book;

import com.betoniarka.biblioteka.author.Author;
import com.betoniarka.biblioteka.author.AuthorRepository;
import com.betoniarka.biblioteka.category.Category;
import com.betoniarka.biblioteka.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookServiceSearchTest {

    @Autowired
    BookService bookService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void getAllWithBlankSearchReturnsAllBooks() {
        persistBook("Harry Potter", "J.K. Rowling");
        persistBook("Hobbit", "J.R.R. Tolkien");

        var allBooks = bookService.getAll("   ");
        assertThat(allBooks).hasSize(2);
    }

    @Test
    void getAllSearchMatchesTitleStartsWithIgnoringCase() {
        persistBook("Harry Potter", "J.K. Rowling");
        persistBook("Hobbit", "J.R.R. Tolkien");
        persistBook("Dune", "Frank Herbert");

        var result = bookService.getAll("hAr");
        assertThat(result).extracting(r -> r.title()).containsExactly("Harry Potter");
    }

    @Test
    void getAllSearchMatchesAuthorStartsWithIgnoringCase() {
        persistBook("Dune", "Frank Herbert");
        persistBook("Hobbit", "J.R.R. Tolkien");

        var result = bookService.getAll(" fr ");
        assertThat(result).extracting(r -> r.title()).containsExactly("Dune");
    }

    @Test
    void getAllSearchIgnoresPunctuationAndWhitespace() {
        persistBook("Harry Potter", "J.K. Rowling");

        var result = bookService.getAll("H,a.r r-y");
        assertThat(result).extracting(r -> r.title()).containsExactly("Harry Potter");
    }

    @Test
    void getAllSearchMatchesCategoryLiterallyIgnoringCaseAndPunctuation() {
        persistBookWithCategory("Dune", "Frank Herbert", "Sci-Fi");

        var result = bookService.getAll(" sci fi ");
        assertThat(result).extracting(r -> r.title()).containsExactly("Dune");

        var nonLiteralResult = bookService.getAll("sci");
        assertThat(nonLiteralResult).isEmpty();
    }

    private void persistBook(String title, String authorName) {
        var author = new Author();
        author.setName(authorName);
        var savedAuthor = authorRepository.save(author);

        var book = new Book();
        book.setTitle(title);
        book.setCount(1);
        book.setAuthor(savedAuthor);

        bookRepository.save(book);
    }

    private void persistBookWithCategory(String title, String authorName, String categoryName) {
        var author = new Author();
        author.setName(authorName);
        var savedAuthor = authorRepository.save(author);

        var category = new Category();
        category.setName(categoryName);
        var savedCategory = categoryRepository.save(category);

        var book = new Book();
        book.setTitle(title);
        book.setCount(1);
        book.setAuthor(savedAuthor);
        book.addCategory(savedCategory);

        bookRepository.save(book);
    }
}
