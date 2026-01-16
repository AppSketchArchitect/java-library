package fr.appsketch.Book;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour BookRepository
 * Utilise JUnit 5 et Mockito pour mocker EntityManager
 */
@ExtendWith(MockitoExtension.class)
class BookRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Book> typedQuery;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepository(entityManager);
    }

    @Test
    void testSave_NewBook_ShouldPersist() {
        // Arrange
        Book newBook = new Book("Le Petit Prince", "Antoine de Saint-Exupéry",
                LocalDate.of(1943, 4, 6), "978-2-07-061275-8", "Conte");

        // Act
        Book result = bookRepository.save(newBook);

        // Assert
        assertNotNull(result);
        verify(entityManager).persist(newBook);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testSave_ExistingBook_ShouldMerge() {
        // Arrange
        Book existingBook = new Book("1984", "George Orwell",
                LocalDate.of(1949, 6, 8), "978-0-452-28423-4", "Science-fiction");
        existingBook.setId(1L);

        when(entityManager.merge(existingBook)).thenReturn(existingBook);

        // Act
        Book result = bookRepository.save(existingBook);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(entityManager).merge(existingBook);
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testFindById_BookExists_ShouldReturnBook() {
        // Arrange
        Long bookId = 1L;
        Book expectedBook = new Book("Le Seigneur des Anneaux", "J.R.R. Tolkien",
                LocalDate.of(1954, 7, 29), "978-2-07-061332-8", "Fantasy");
        expectedBook.setId(bookId);

        when(entityManager.find(Book.class, bookId)).thenReturn(expectedBook);

        // Act
        Optional<Book> result = bookRepository.findById(bookId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(bookId, result.get().getId());
        assertEquals("Le Seigneur des Anneaux", result.get().getTitre());
        verify(entityManager).find(Book.class, bookId);
    }

    @Test
    void testFindById_BookNotFound_ShouldReturnEmpty() {
        // Arrange
        Long bookId = 999L;
        when(entityManager.find(Book.class, bookId)).thenReturn(null);

        // Act
        Optional<Book> result = bookRepository.findById(bookId);

        // Assert
        assertFalse(result.isPresent());
        verify(entityManager).find(Book.class, bookId);
    }

    @Test
    void testFindAll_ShouldReturnAllBooks() {
        // Arrange
        Book book1 = new Book("Livre 1", "Auteur 1", LocalDate.now(), "ISBN1", "Cat1");
        Book book2 = new Book("Livre 2", "Auteur 2", LocalDate.now(), "ISBN2", "Cat2");
        List<Book> expectedBooks = Arrays.asList(book1, book2);

        when(entityManager.createQuery("SELECT b FROM Book b", Book.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedBooks, result);
        verify(entityManager).createQuery("SELECT b FROM Book b", Book.class);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindAll_EmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(entityManager.createQuery("SELECT b FROM Book b", Book.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        // Act
        List<Book> result = bookRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).createQuery("SELECT b FROM Book b", Book.class);
    }

    @Test
    void testFindByIsbn_BookExists_ShouldReturnBook() {
        // Arrange
        String isbn = "978-2-07-061332-8";
        Book expectedBook = new Book("Harry Potter", "J.K. Rowling",
                LocalDate.of(1997, 6, 26), isbn, "Fantasy");

        when(entityManager.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("isbn", isbn)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(expectedBook));

        // Act
        Optional<Book> result = bookRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(isbn, result.get().getIsbn());
        assertEquals("Harry Potter", result.get().getTitre());
        verify(typedQuery).setParameter("isbn", isbn);
    }

    @Test
    void testFindByIsbn_BookNotFound_ShouldReturnEmpty() {
        // Arrange
        String isbn = "978-0-000-00000-0";

        when(entityManager.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("isbn", isbn)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        // Act
        Optional<Book> result = bookRepository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());
        verify(typedQuery).setParameter("isbn", isbn);
    }

    @Test
    void testExistsByIsbn_BookExists_ShouldReturnTrue() {
        // Arrange
        String isbn = "978-2-07-061332-8";

        when(entityManager.createQuery("SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn", Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter("isbn", isbn)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(1L);

        // Act
        boolean result = bookRepository.existsByIsbn(isbn);

        // Assert
        assertTrue(result);
        verify(longTypedQuery).setParameter("isbn", isbn);
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testExistsByIsbn_BookNotFound_ShouldReturnFalse() {
        // Arrange
        String isbn = "978-0-000-00000-0";

        when(entityManager.createQuery("SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn", Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter("isbn", isbn)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(0L);

        // Act
        boolean result = bookRepository.existsByIsbn(isbn);

        // Assert
        assertFalse(result);
        verify(longTypedQuery).setParameter("isbn", isbn);
    }

    @Test
    void testDeleteById_BookExists_ShouldRemove() {
        // Arrange
        Long bookId = 1L;
        Book bookToDelete = new Book("Livre à supprimer", "Auteur",
                LocalDate.now(), "ISBN123", "Categorie");
        bookToDelete.setId(bookId);

        when(entityManager.find(Book.class, bookId)).thenReturn(bookToDelete);

        // Act
        bookRepository.deleteById(bookId);

        // Assert
        verify(entityManager).find(Book.class, bookId);
        verify(entityManager).remove(bookToDelete);
    }

    @Test
    void testDeleteById_BookNotFound_ShouldNotRemove() {
        // Arrange
        Long bookId = 999L;
        when(entityManager.find(Book.class, bookId)).thenReturn(null);

        // Act
        bookRepository.deleteById(bookId);

        // Assert
        verify(entityManager).find(Book.class, bookId);
        verify(entityManager, never()).remove(any());
    }
}
