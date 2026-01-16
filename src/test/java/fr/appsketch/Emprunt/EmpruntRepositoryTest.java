package fr.appsketch.Emprunt;

import fr.appsketch.Book.Book;
import fr.appsketch.User.User;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmpruntRepository
 * Utilise JUnit 5 et Mockito pour mocker EntityManager
 */
@ExtendWith(MockitoExtension.class)
class EmpruntRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Emprunt> typedQuery;

    private EmpruntRepository empruntRepository;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        empruntRepository = new EmpruntRepository(entityManager);

        // Créer des objets de test
        testUser = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");
        testUser.setId(1L);

        testBook = new Book("1984", "George Orwell", LocalDate.of(1949, 6, 8),
                "978-0-452-28423-4", "Science-fiction");
        testBook.setId(1L);
    }

    @Test
    void testSave_NewEmprunt_ShouldPersist() {
        // Arrange
        Emprunt newEmprunt = new Emprunt(testUser, testBook, LocalDate.now());

        // Act
        Emprunt result = empruntRepository.save(newEmprunt);

        // Assert
        assertNotNull(result);
        verify(entityManager).persist(newEmprunt);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testSave_ExistingEmprunt_ShouldMerge() {
        // Arrange
        Emprunt existingEmprunt = new Emprunt(testUser, testBook, LocalDate.now());
        existingEmprunt.setId(1L);

        when(entityManager.merge(existingEmprunt)).thenReturn(existingEmprunt);

        // Act
        Emprunt result = empruntRepository.save(existingEmprunt);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(entityManager).merge(existingEmprunt);
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testFindById_EmpruntExists_ShouldReturnEmprunt() {
        // Arrange
        Long empruntId = 1L;
        Emprunt expectedEmprunt = new Emprunt(testUser, testBook, LocalDate.now());
        expectedEmprunt.setId(empruntId);

        when(entityManager.find(Emprunt.class, empruntId)).thenReturn(expectedEmprunt);

        // Act
        Optional<Emprunt> result = empruntRepository.findById(empruntId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(empruntId, result.get().getId());
        assertEquals(testUser, result.get().getUser());
        assertEquals(testBook, result.get().getBook());
        verify(entityManager).find(Emprunt.class, empruntId);
    }

    @Test
    void testFindById_EmpruntNotFound_ShouldReturnEmpty() {
        // Arrange
        Long empruntId = 999L;
        when(entityManager.find(Emprunt.class, empruntId)).thenReturn(null);

        // Act
        Optional<Emprunt> result = empruntRepository.findById(empruntId);

        // Assert
        assertFalse(result.isPresent());
        verify(entityManager).find(Emprunt.class, empruntId);
    }

    @Test
    void testFindAll_ShouldReturnAllEmprunts() {
        // Arrange
        Emprunt emprunt1 = new Emprunt(testUser, testBook, LocalDate.now());
        Emprunt emprunt2 = new Emprunt(testUser, testBook, LocalDate.now().minusDays(5));
        List<Emprunt> expectedEmprunts = Arrays.asList(emprunt1, emprunt2);

        when(entityManager.createQuery("SELECT e FROM Emprunt e", Emprunt.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedEmprunts);

        // Act
        List<Emprunt> result = empruntRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedEmprunts, result);
        verify(entityManager).createQuery("SELECT e FROM Emprunt e", Emprunt.class);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindAll_EmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(entityManager.createQuery("SELECT e FROM Emprunt e", Emprunt.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        // Act
        List<Emprunt> result = empruntRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).createQuery("SELECT e FROM Emprunt e", Emprunt.class);
    }

    @Test
    void testFindByUser_ShouldReturnUserEmprunts() {
        // Arrange
        Emprunt emprunt1 = new Emprunt(testUser, testBook, LocalDate.now());
        Emprunt emprunt2 = new Emprunt(testUser, testBook, LocalDate.now().minusDays(3));
        List<Emprunt> expectedEmprunts = Arrays.asList(emprunt1, emprunt2);

        when(entityManager.createQuery("SELECT e FROM Emprunt e WHERE e.user = :user", Emprunt.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("user", testUser)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedEmprunts);

        // Act
        List<Emprunt> result = empruntRepository.findByUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(typedQuery).setParameter("user", testUser);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindByBook_ShouldReturnBookEmprunts() {
        // Arrange
        Emprunt emprunt1 = new Emprunt(testUser, testBook, LocalDate.now());
        List<Emprunt> expectedEmprunts = List.of(emprunt1);

        when(entityManager.createQuery("SELECT e FROM Emprunt e WHERE e.book = :book", Emprunt.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("book", testBook)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedEmprunts);

        // Act
        List<Emprunt> result = empruntRepository.findByBook(testBook);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery).setParameter("book", testBook);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindByUserId_ShouldReturnUserEmprunts() {
        // Arrange
        Long userId = 1L;
        Emprunt emprunt1 = new Emprunt(testUser, testBook, LocalDate.now());
        List<Emprunt> expectedEmprunts = List.of(emprunt1);

        when(entityManager.createQuery("SELECT e FROM Emprunt e WHERE e.user.id = :userId", Emprunt.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", userId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedEmprunts);

        // Act
        List<Emprunt> result = empruntRepository.findByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery).setParameter("userId", userId);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindByBookId_ShouldReturnBookEmprunts() {
        // Arrange
        Long bookId = 1L;
        Emprunt emprunt1 = new Emprunt(testUser, testBook, LocalDate.now());
        List<Emprunt> expectedEmprunts = List.of(emprunt1);

        when(entityManager.createQuery("SELECT e FROM Emprunt e WHERE e.book.id = :bookId", Emprunt.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("bookId", bookId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedEmprunts);

        // Act
        List<Emprunt> result = empruntRepository.findByBookId(bookId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(typedQuery).setParameter("bookId", bookId);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindEmpruntsEnCoursByBook_ShouldReturnActiveEmprunts() {
        // Arrange
        Emprunt empruntEnCours = new Emprunt(testUser, testBook, LocalDate.now());
        empruntEnCours.setEtat(EtatEmprunt.EN_COURS);
        List<Emprunt> expectedEmprunts = List.of(empruntEnCours);

        when(entityManager.createQuery("SELECT e FROM Emprunt e WHERE e.book = :book AND e.etat = :etat", Emprunt.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("book", testBook)).thenReturn(typedQuery);
        when(typedQuery.setParameter("etat", EtatEmprunt.EN_COURS)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedEmprunts);

        // Act
        List<Emprunt> result = empruntRepository.findEmpruntsEnCoursByBook(testBook);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EtatEmprunt.EN_COURS, result.get(0).getEtat());
        verify(typedQuery).setParameter("book", testBook);
        verify(typedQuery).setParameter("etat", EtatEmprunt.EN_COURS);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindEmpruntsEnCoursByBook_NoActiveEmprunts_ShouldReturnEmptyList() {
        // Arrange
        when(entityManager.createQuery("SELECT e FROM Emprunt e WHERE e.book = :book AND e.etat = :etat", Emprunt.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("book", testBook)).thenReturn(typedQuery);
        when(typedQuery.setParameter("etat", EtatEmprunt.EN_COURS)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        // Act
        List<Emprunt> result = empruntRepository.findEmpruntsEnCoursByBook(testBook);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteById_EmpruntExists_ShouldRemove() {
        // Arrange
        Long empruntId = 1L;
        Emprunt empruntToDelete = new Emprunt(testUser, testBook, LocalDate.now());
        empruntToDelete.setId(empruntId);

        when(entityManager.find(Emprunt.class, empruntId)).thenReturn(empruntToDelete);

        // Act
        empruntRepository.deleteById(empruntId);

        // Assert
        verify(entityManager).find(Emprunt.class, empruntId);
        verify(entityManager).remove(empruntToDelete);
    }

    @Test
    void testDeleteById_EmpruntNotFound_ShouldNotRemove() {
        // Arrange
        Long empruntId = 999L;
        when(entityManager.find(Emprunt.class, empruntId)).thenReturn(null);

        // Act
        empruntRepository.deleteById(empruntId);

        // Assert
        verify(entityManager).find(Emprunt.class, empruntId);
        verify(entityManager, never()).remove(any());
    }
}
