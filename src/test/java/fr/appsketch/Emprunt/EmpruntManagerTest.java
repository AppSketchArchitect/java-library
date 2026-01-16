package fr.appsketch.Emprunt;

import fr.appsketch.Book.Book;
import fr.appsketch.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests unitaires pour EmpruntManager
 * Utilise JUnit 5 et Mockito pour mocker les dépendances
 */
@ExtendWith(MockitoExtension.class)
class EmpruntManagerTest {

    @Mock
    private EmpruntRepository empruntRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction transaction;

    private EmpruntManager empruntManager;

    @BeforeEach
    void setUp() {
        // Configurer le mock de transaction avec lenient pour éviter UnnecessaryStubbing
        lenient().when(entityManager.getTransaction()).thenReturn(transaction);
        // Simuler le comportement réel : la transaction est active après begin() et inactive après commit()/rollback()
        lenient().when(transaction.isActive()).thenReturn(false);
        lenient().doAnswer(invocation -> {
            lenient().when(transaction.isActive()).thenReturn(true);
            return null;
        }).when(transaction).begin();
        lenient().doAnswer(invocation -> {
            lenient().when(transaction.isActive()).thenReturn(false);
            return null;
        }).when(transaction).commit();
        lenient().doAnswer(invocation -> {
            lenient().when(transaction.isActive()).thenReturn(false);
            return null;
        }).when(transaction).rollback();

        empruntManager = new EmpruntManager(empruntRepository, entityManager);
    }

    @Test
    void testEmprunterLivre_Success() {
        // Arrange
        User user = new User("Dupont", "Jean", "jean@test.com", "password");
        user.setId(1L);

        Book book = new Book("Le Seigneur des Anneaux", "Tolkien", LocalDate.now(), "123", "Fantasy");
        book.setId(1L);

        Emprunt expectedEmprunt = new Emprunt(user, book, LocalDate.now());
        expectedEmprunt.setId(1L);

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(Collections.emptyList());
        when(empruntRepository.save(any(Emprunt.class))).thenReturn(expectedEmprunt);

        // Act
        Emprunt result = empruntManager.emprunterLivre(user, book);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEmprunt.getId(), result.getId());
        verify(transaction).begin();
        verify(transaction).commit();
        verify(empruntRepository).save(any(Emprunt.class));
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testEmprunterLivre_UserNull_ThrowsException() {
        // Arrange
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            empruntManager.emprunterLivre(null, book);
        });

        verify(empruntRepository, never()).save(any(Emprunt.class));
    }

    @Test
    void testEmprunterLivre_BookNull_ThrowsException() {
        // Arrange
        User user = new User("Nom", "Prenom", "email@test.com", "password");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            empruntManager.emprunterLivre(user, null);
        });

        verify(empruntRepository, never()).save(any(Emprunt.class));
    }

    @Test
    void testEmprunterLivre_LivreDejaEmprunte_ThrowsException() {
        // Arrange
        User user = new User("Dupont", "Jean", "jean@test.com", "password");
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");

        Emprunt existingEmprunt = new Emprunt(user, book, LocalDate.now().minusDays(5));
        existingEmprunt.setEtat(EtatEmprunt.EN_COURS);

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(List.of(existingEmprunt));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            empruntManager.emprunterLivre(user, book);
        });

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(empruntRepository, never()).save(any(Emprunt.class));
    }

    @Test
    void testEmprunterLivre_ExceptionLorsDeLaSauvegarde_Rollback() {
        // Arrange
        User user = new User("Nom", "Prenom", "email@test.com", "password");
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(Collections.emptyList());
        when(empruntRepository.save(any(Emprunt.class))).thenThrow(new RuntimeException("Erreur DB"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            empruntManager.emprunterLivre(user, book);
        });

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    void testRendreLivre_Success() {
        // Arrange
        User user = new User("Dupont", "Jean", "jean@test.com", "password");
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");

        Emprunt emprunt = new Emprunt(user, book, LocalDate.now().minusDays(7));
        emprunt.setEtat(EtatEmprunt.EN_COURS);
        emprunt.setId(1L);

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(List.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class))).thenReturn(emprunt);

        // Act
        empruntManager.rendreLivre(book);

        // Assert
        verify(transaction).begin();
        verify(transaction).commit();
        verify(empruntRepository).save(any(Emprunt.class));
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testRendreLivre_BookNull_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            empruntManager.rendreLivre(null);
        });

        verify(empruntRepository, never()).save(any(Emprunt.class));
    }

    @Test
    void testRendreLivre_LivreNonEmprunte_ThrowsException() {
        // Arrange
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            empruntManager.rendreLivre(book);
        });

        verify(empruntRepository, never()).save(any(Emprunt.class));
    }

    @Test
    void testRendreLivre_ExceptionLorsDeLaSauvegarde_Rollback() {
        // Arrange
        User user = new User("Nom", "Prenom", "email@test.com", "password");
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");

        Emprunt emprunt = new Emprunt(user, book, LocalDate.now().minusDays(7));
        emprunt.setEtat(EtatEmprunt.EN_COURS);

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(List.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class))).thenThrow(new RuntimeException("Erreur DB"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            empruntManager.rendreLivre(book);
        });

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    void testListerTousLesEmprunts() {
        // Arrange
        User user1 = new User("Dupont", "Jean", "jean@test.com", "pass1");
        User user2 = new User("Martin", "Marie", "marie@test.com", "pass2");

        Book book1 = new Book("Livre 1", "Auteur 1", LocalDate.now(), "111", "Cat1");
        Book book2 = new Book("Livre 2", "Auteur 2", LocalDate.now(), "222", "Cat2");

        List<Emprunt> emprunts = Arrays.asList(
                new Emprunt(user1, book1, LocalDate.now()),
                new Emprunt(user2, book2, LocalDate.now())
        );

        when(empruntRepository.findAll()).thenReturn(emprunts);

        // Act
        List<Emprunt> result = empruntManager.listerTousLesEmprunts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(empruntRepository).findAll();
    }

    @Test
    void testListerEmpruntsParUtilisateur() {
        // Arrange
        Long userId = 1L;
        User user = new User("Dupont", "Jean", "jean@test.com", "password");
        user.setId(userId);

        Book book1 = new Book("Livre 1", "Auteur 1", LocalDate.now(), "111", "Cat1");
        Book book2 = new Book("Livre 2", "Auteur 2", LocalDate.now(), "222", "Cat2");

        List<Emprunt> emprunts = Arrays.asList(
                new Emprunt(user, book1, LocalDate.now()),
                new Emprunt(user, book2, LocalDate.now())
        );

        when(empruntRepository.findByUserId(userId)).thenReturn(emprunts);

        // Act
        List<Emprunt> result = empruntManager.listerEmpruntsParUtilisateur(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(empruntRepository).findByUserId(userId);
    }

    @Test
    void testListerEmpruntsParLivre() {
        // Arrange
        Long bookId = 1L;
        Book book = new Book("Le Seigneur des Anneaux", "Tolkien", LocalDate.now(), "123", "Fantasy");
        book.setId(bookId);

        User user1 = new User("Dupont", "Jean", "jean@test.com", "pass1");
        User user2 = new User("Martin", "Marie", "marie@test.com", "pass2");

        List<Emprunt> emprunts = Arrays.asList(
                new Emprunt(user1, book, LocalDate.now().minusDays(30)),
                new Emprunt(user2, book, LocalDate.now())
        );

        when(empruntRepository.findByBookId(bookId)).thenReturn(emprunts);

        // Act
        List<Emprunt> result = empruntManager.listerEmpruntsParLivre(bookId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(empruntRepository).findByBookId(bookId);
    }

    @Test
    void testGetEmpruntEnCours_EmpruntTrouve() {
        // Arrange
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");
        User user = new User("Nom", "Prenom", "email@test.com", "password");

        Emprunt emprunt = new Emprunt(user, book, LocalDate.now());
        emprunt.setEtat(EtatEmprunt.EN_COURS);
        emprunt.setId(1L);

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(List.of(emprunt));

        // Act
        Optional<Emprunt> result = empruntManager.getEmpruntEnCours(book);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(emprunt.getId(), result.get().getId());
        verify(empruntRepository).findEmpruntsEnCoursByBook(book);
    }

    @Test
    void testGetEmpruntEnCours_EmpruntNonTrouve() {
        // Arrange
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(Collections.emptyList());

        // Act
        Optional<Emprunt> result = empruntManager.getEmpruntEnCours(book);

        // Assert
        assertFalse(result.isPresent());
        verify(empruntRepository).findEmpruntsEnCoursByBook(book);
    }

    @Test
    void testGetEmpruntEnCours_PlusieurEmprunts_RetourneLePremier() {
        // Arrange
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");
        User user1 = new User("User1", "Prenom1", "user1@test.com", "pass");
        User user2 = new User("User2", "Prenom2", "user2@test.com", "pass");

        Emprunt emprunt1 = new Emprunt(user1, book, LocalDate.now().minusDays(10));
        emprunt1.setId(1L);
        emprunt1.setEtat(EtatEmprunt.EN_COURS);

        Emprunt emprunt2 = new Emprunt(user2, book, LocalDate.now());
        emprunt2.setId(2L);
        emprunt2.setEtat(EtatEmprunt.EN_COURS);

        when(empruntRepository.findEmpruntsEnCoursByBook(book)).thenReturn(Arrays.asList(emprunt1, emprunt2));

        // Act
        Optional<Emprunt> result = empruntManager.getEmpruntEnCours(book);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(empruntRepository).findEmpruntsEnCoursByBook(book);
    }

    @Test
    void testGetEmpruntEnCours_BookNull() {
        // Act
        Optional<Emprunt> result = empruntManager.getEmpruntEnCours(null);

        // Assert
        assertFalse(result.isPresent());
        verify(empruntRepository, never()).findEmpruntsEnCoursByBook(any());
    }

    @Test
    void testListerTousLesEmprunts_ListeVide() {
        // Arrange
        when(empruntRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Emprunt> result = empruntManager.listerTousLesEmprunts();

        // Assert
        assertTrue(result.isEmpty());
        verify(empruntRepository).findAll();
    }

    @Test
    void testListerEmpruntsParUtilisateur_UserNull() {
        // Act
        List<Emprunt> result = empruntManager.listerEmpruntsParUtilisateur(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(empruntRepository, never()).findByUserId(any());
    }

    @Test
    void testListerEmpruntsParUtilisateur_AucunEmprunt() {
        // Arrange
        Long userId = 1L;
        when(empruntRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        List<Emprunt> result = empruntManager.listerEmpruntsParUtilisateur(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(empruntRepository).findByUserId(userId);
    }

    @Test
    void testListerEmpruntsParLivre_BookNull() {
        // Act
        List<Emprunt> result = empruntManager.listerEmpruntsParLivre(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(empruntRepository, never()).findByBookId(any());
    }

    @Test
    void testListerEmpruntsParLivre_AucunEmprunt() {
        // Arrange
        Long bookId = 1L;
        when(empruntRepository.findByBookId(bookId)).thenReturn(Arrays.asList());

        // Act
        List<Emprunt> result = empruntManager.listerEmpruntsParLivre(bookId);

        // Assert
        assertTrue(result.isEmpty());
        verify(empruntRepository).findByBookId(bookId);
    }
}
