package fr.appsketch.Book;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests unitaires pour BookManager
 * Utilise JUnit 5 et Mockito pour mocker les dépendances
 */
@ExtendWith(MockitoExtension.class)
class BookManagerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction transaction;

    private BookManager bookManager;

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

        bookManager = new BookManager(bookRepository, entityManager);
    }

    @Test
    void testAjouterLivre_Success() {
        // Arrange
        String titre = "Le Seigneur des Anneaux";
        String auteur = "J.R.R. Tolkien";
        LocalDate datePublication = LocalDate.of(1954, 7, 29);
        String isbn = "978-2-07-061332-8";
        String categorie = "Fantasy";

        Book expectedBook = new Book(titre, auteur, datePublication, isbn, categorie);
        expectedBook.setId(1L);

        when(bookRepository.existsByIsbn(isbn)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(expectedBook);

        // Act
        Book result = bookManager.ajouterLivre(titre, auteur, datePublication, isbn, categorie);

        // Assert
        assertNotNull(result);
        assertEquals(titre, result.getTitre());
        assertEquals(auteur, result.getAuteur());
        verify(transaction).begin();
        verify(transaction).commit();
        verify(bookRepository).save(any(Book.class));
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testAjouterLivre_TitreNull_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bookManager.ajouterLivre(null, "Auteur", LocalDate.now(), "123", "Categorie");
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testAjouterLivre_TitreVide_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bookManager.ajouterLivre("   ", "Auteur", LocalDate.now(), "123", "Categorie");
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testAjouterLivre_AuteurNull_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bookManager.ajouterLivre("Titre", null, LocalDate.now(), "123", "Categorie");
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testAjouterLivre_IsbnExisteDeja_ThrowsException() {
        // Arrange
        String isbn = "978-2-07-061332-8";
        when(bookRepository.existsByIsbn(isbn)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bookManager.ajouterLivre("Titre", "Auteur", LocalDate.now(), isbn, "Categorie");
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testAjouterLivre_ExceptionLorsDeLaSauvegarde_Rollback() {
        // Arrange
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("Erreur DB"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bookManager.ajouterLivre("Titre", "Auteur", LocalDate.now(), "123", "Categorie");
        });

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    void testModifierLivre_Success() {
        // Arrange
        Long id = 1L;
        Book existingBook = new Book("Ancien Titre", "Ancien Auteur", LocalDate.now(), "123", "Cat");
        existingBook.setId(id);

        Book updatedBook = new Book("Nouveau Titre", "Nouvel Auteur", LocalDate.now(), "456", "Nouvelle Cat");
        updatedBook.setId(id);

        when(bookRepository.findById(id)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        // Act
        Book result = bookManager.modifierLivre(id, "Nouveau Titre", "Nouvel Auteur",
                LocalDate.now(), "456", "Nouvelle Cat");

        // Assert
        assertNotNull(result);
        verify(transaction).begin();
        verify(transaction).commit();
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testModifierLivre_LivreNonTrouve_ThrowsException() {
        // Arrange
        Long id = 999L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bookManager.modifierLivre(id, "Titre", "Auteur", LocalDate.now(), "123", "Cat");
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testSupprimerLivre_Success() {
        // Arrange
        Long id = 1L;
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");
        book.setId(id);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // Act
        bookManager.supprimerLivre(id);

        // Assert
        verify(transaction).begin();
        verify(transaction).commit();
        verify(bookRepository).deleteById(id);
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testSupprimerLivre_LivreNonTrouve_ThrowsException() {
        // Arrange
        Long id = 999L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bookManager.supprimerLivre(id);
        });

        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    void testTrouverParId_LivreTrouve() {
        // Arrange
        Long id = 1L;
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "123", "Cat");
        book.setId(id);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // Act
        Optional<Book> result = bookManager.trouverParId(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(bookRepository).findById(id);
    }

    @Test
    void testTrouverParId_LivreNonTrouve() {
        // Arrange
        Long id = 999L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Book> result = bookManager.trouverParId(id);

        // Assert
        assertFalse(result.isPresent());
        verify(bookRepository).findById(id);
    }

    @Test
    void testListerTousLesLivres() {
        // Arrange
        List<Book> books = Arrays.asList(
                new Book("Livre 1", "Auteur 1", LocalDate.now(), "111", "Cat1"),
                new Book("Livre 2", "Auteur 2", LocalDate.now(), "222", "Cat2")
        );

        when(bookRepository.findAll()).thenReturn(books);

        // Act
        List<Book> result = bookManager.listerTousLesLivres();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository).findAll();
    }

    @Test
    void testListerLivresDisponibles() {
        // Arrange
        Book book1 = new Book("Livre 1", "Auteur 1", LocalDate.now(), "111", "Cat1");

        Book book2 = new Book("Livre 2", "Auteur 2", LocalDate.now(), "222", "Cat2");
        // Créer un emprunt en cours pour book2
        fr.appsketch.User.User user = new fr.appsketch.User.User("Nom", "Prenom", "email@test.com", "password");
        fr.appsketch.Emprunt.Emprunt emprunt = new fr.appsketch.Emprunt.Emprunt(user, book2, LocalDate.now());
        emprunt.setEtat(fr.appsketch.Emprunt.EtatEmprunt.EN_COURS);
        book2.addEmprunt(emprunt);

        Book book3 = new Book("Livre 3", "Auteur 3", LocalDate.now(), "333", "Cat3");

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        // Act
        List<Book> result = bookManager.listerLivresDisponibles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(Book::isEmprunte));
    }

    @Test
    void testListerLivresEmpruntes() {
        // Arrange
        Book book1 = new Book("Livre 1", "Auteur 1", LocalDate.now(), "111", "Cat1");

        Book book2 = new Book("Livre 2", "Auteur 2", LocalDate.now(), "222", "Cat2");
        // Créer un emprunt en cours pour book2
        fr.appsketch.User.User user2 = new fr.appsketch.User.User("Nom2", "Prenom2", "email2@test.com", "password");
        fr.appsketch.Emprunt.Emprunt emprunt2 = new fr.appsketch.Emprunt.Emprunt(user2, book2, LocalDate.now());
        emprunt2.setEtat(fr.appsketch.Emprunt.EtatEmprunt.EN_COURS);
        book2.addEmprunt(emprunt2);

        Book book3 = new Book("Livre 3", "Auteur 3", LocalDate.now(), "333", "Cat3");
        // Créer un emprunt en cours pour book3
        fr.appsketch.User.User user3 = new fr.appsketch.User.User("Nom3", "Prenom3", "email3@test.com", "password");
        fr.appsketch.Emprunt.Emprunt emprunt3 = new fr.appsketch.Emprunt.Emprunt(user3, book3, LocalDate.now());
        emprunt3.setEtat(fr.appsketch.Emprunt.EtatEmprunt.EN_COURS);
        book3.addEmprunt(emprunt3);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        // Act
        List<Book> result = bookManager.listerLivresEmpruntes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Book::isEmprunte));
    }

    @Test
    void testRechercherParTitre_TitreValide() {
        // Arrange
        Book book1 = new Book("Le Seigneur des Anneaux", "Tolkien", LocalDate.now(), "111", "Cat1");
        Book book2 = new Book("Harry Potter", "Rowling", LocalDate.now(), "222", "Cat2");
        Book book3 = new Book("Le Hobbit", "Tolkien", LocalDate.now(), "333", "Cat3");

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        // Act
        List<Book> result = bookManager.rechercherParTitre("Le");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getTitre().toLowerCase().contains("le")));
    }

    @Test
    void testRechercherParTitre_TitreNull() {
        // Act
        List<Book> result = bookManager.rechercherParTitre(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void testRechercherParAuteur_AuteurValide() {
        // Arrange
        Book book1 = new Book("Le Seigneur des Anneaux", "Tolkien", LocalDate.now(), "111", "Cat1");
        Book book2 = new Book("Harry Potter", "Rowling", LocalDate.now(), "222", "Cat2");
        Book book3 = new Book("Le Hobbit", "Tolkien", LocalDate.now(), "333", "Cat3");

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        // Act
        List<Book> result = bookManager.rechercherParAuteur("Tolkien");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getAuteur().toLowerCase().contains("tolkien")));
    }

    @Test
    void testRechercherParCategorie_CategorieValide() {
        // Arrange
        Book book1 = new Book("Livre 1", "Auteur 1", LocalDate.now(), "111", "Fantasy");
        Book book2 = new Book("Livre 2", "Auteur 2", LocalDate.now(), "222", "Science-Fiction");
        Book book3 = new Book("Livre 3", "Auteur 3", LocalDate.now(), "333", "Fantasy");

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        // Act
        List<Book> result = bookManager.rechercherParCategorie("Fantasy");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getCategorie().toLowerCase().contains("fantasy")));
    }

    @Test
    void testIsbnExiste_IsbnExiste() {
        // Arrange
        String isbn = "978-2-07-061332-8";
        when(bookRepository.existsByIsbn(isbn)).thenReturn(true);

        // Act
        boolean result = bookManager.isbnExiste(isbn);

        // Assert
        assertTrue(result);
        verify(bookRepository).existsByIsbn(isbn);
    }

    @Test
    void testIsbnExiste_IsbnNexistePas() {
        // Arrange
        String isbn = "978-2-07-061332-8";
        when(bookRepository.existsByIsbn(isbn)).thenReturn(false);

        // Act
        boolean result = bookManager.isbnExiste(isbn);

        // Assert
        assertFalse(result);
        verify(bookRepository).existsByIsbn(isbn);
    }

    @Test
    void testIsbnExiste_IsbnNull() {
        // Act
        boolean result = bookManager.isbnExiste(null);

        // Assert
        assertFalse(result);
        verify(bookRepository, never()).existsByIsbn(anyString());
    }

    @Test
    void testExporterVersJson_Success() throws IOException {
        // Arrange
        Book book1 = new Book("Livre 1", "Auteur 1", LocalDate.of(2020, 1, 1), "111", "Cat1");
        Book book2 = new Book("Livre 2", "Auteur 2", LocalDate.of(2021, 2, 2), "222", "Cat2");

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        File tempFile = File.createTempFile("test_export", ".json");
        tempFile.deleteOnExit();

        // Act
        bookManager.exporterVersJson(tempFile.getAbsolutePath());

        // Assert
        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
        verify(bookRepository).findAll();
    }

    @Test
    void testImporterDepuisJson_Success() throws IOException {
        // Arrange
        File tempFile = File.createTempFile("test_import", ".json");
        tempFile.deleteOnExit();

        String jsonContent = """
                [
                  {
                    "titre": "Livre Test 1",
                    "auteur": "Auteur Test 1",
                    "datePublication": "2020-01-01",
                    "isbn": "111",
                    "categorie": "Cat1"
                  },
                  {
                    "titre": "Livre Test 2",
                    "auteur": "Auteur Test 2",
                    "datePublication": "2021-02-02",
                    "isbn": "222",
                    "categorie": "Cat2"
                  }
                ]
                """;

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(jsonContent);
        }

        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        int result = bookManager.importerDepuisJson(tempFile.getAbsolutePath());

        // Assert
        assertEquals(2, result);
        verify(transaction).begin();
        verify(transaction).commit();
        verify(bookRepository, times(2)).save(any(Book.class));
    }

    @Test
    void testImporterDepuisJson_IsbnExiste_IgnoreLivre() throws IOException {
        // Arrange
        File tempFile = File.createTempFile("test_import", ".json");
        tempFile.deleteOnExit();

        String jsonContent = """
                [
                  {
                    "titre": "Livre Test 1",
                    "auteur": "Auteur Test 1",
                    "datePublication": "2020-01-01",
                    "isbn": "111",
                    "categorie": "Cat1"
                  }
                ]
                """;

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(jsonContent);
        }

        when(bookRepository.existsByIsbn("111")).thenReturn(true);

        // Act
        int result = bookManager.importerDepuisJson(tempFile.getAbsolutePath());

        // Assert
        assertEquals(0, result);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testImporterDepuisJson_FichierInexistant() throws IOException {
        // Act
        int result = bookManager.importerDepuisJson("fichier_inexistant.json");

        // Assert
        assertEquals(0, result);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testExporterVersJson_ListeVide() throws IOException {
        // Arrange
        when(bookRepository.findAll()).thenReturn(List.of());

        File tempFile = File.createTempFile("test_export_vide", ".json");
        tempFile.deleteOnExit();

        // Act
        bookManager.exporterVersJson(tempFile.getAbsolutePath());

        // Assert
        assertTrue(tempFile.exists());
        verify(bookRepository).findAll();
    }

    @Test
    void testRechercherParTitre_TitreVide() {
        // Act
        List<Book> result = bookManager.rechercherParTitre("");

        // Assert
        assertTrue(result.isEmpty());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void testRechercherParAuteur_AuteurNull() {
        // Act
        List<Book> result = bookManager.rechercherParAuteur(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void testRechercherParAuteur_AuteurVide() {
        // Act
        List<Book> result = bookManager.rechercherParAuteur("");

        // Assert
        assertTrue(result.isEmpty());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void testRechercherParCategorie_CategorieNull() {
        // Act
        List<Book> result = bookManager.rechercherParCategorie(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void testRechercherParCategorie_CategorieVide() {
        // Act
        List<Book> result = bookManager.rechercherParCategorie("");

        // Assert
        assertTrue(result.isEmpty());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void testAjouterLivre_AvecIsbnVide() {
        // Arrange
        String titre = "Livre sans ISBN";
        String auteur = "Auteur Test";
        String isbn = "";

        Book expectedBook = new Book(titre, auteur, null, isbn, "Test");
        expectedBook.setId(1L);

        when(bookRepository.save(any(Book.class))).thenReturn(expectedBook);

        // Act
        Book result = bookManager.ajouterLivre(titre, auteur, null, isbn, "Test");

        // Assert
        assertNotNull(result);
        verify(bookRepository, never()).existsByIsbn(anyString());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testAjouterLivre_AvecIsbnNull() {
        // Arrange
        String titre = "Livre sans ISBN";
        String auteur = "Auteur Test";

        Book expectedBook = new Book(titre, auteur, null, null, "Test");
        expectedBook.setId(1L);

        when(bookRepository.save(any(Book.class))).thenReturn(expectedBook);

        // Act
        Book result = bookManager.ajouterLivre(titre, auteur, null, null, "Test");

        // Assert
        assertNotNull(result);
        verify(bookRepository, never()).existsByIsbn(anyString());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testListerTousLesLivres_ListeVide() {
        // Arrange
        when(bookRepository.findAll()).thenReturn(List.of());

        // Act
        List<Book> result = bookManager.listerTousLesLivres();

        // Assert
        assertTrue(result.isEmpty());
        verify(bookRepository).findAll();
    }
}
