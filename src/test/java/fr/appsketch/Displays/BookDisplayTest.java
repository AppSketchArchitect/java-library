package fr.appsketch.Displays;

import fr.appsketch.Book.Book;
import fr.appsketch.Book.BookManager;
import fr.appsketch.Emprunt.Emprunt;
import fr.appsketch.Emprunt.EmpruntManager;
import fr.appsketch.User.User;
import fr.appsketch.User.UserManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour BookDisplay
 * Utilise JUnit 5 et Mockito pour mocker les dépendances
 */
@ExtendWith(MockitoExtension.class)
class BookDisplayTest {

    @Mock
    private BookManager bookManager;

    @Mock
    private EmpruntManager empruntManager;

    @Mock
    private UserManager userManager;

    private BookDisplay bookDisplay;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        bookDisplay = new BookDisplay(bookManager, empruntManager, userManager);
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testAfficherMenu_ChoixQuitter_ShouldExit() {
        // Arrange
        String input = "0\n";
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("GESTION DES LIVRES - MENU"));
        assertTrue(output.contains("Retour au menu principal"));
    }

    @Test
    void testAfficherMenu_ChoixInvalide_ShouldShowError() {
        // Arrange
        String input = "99\n0\n";
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Choix invalide"));
    }

    @Test
    void testAfficherMenu_ChoixListerLivres_ShouldCallManager() {
        // Arrange
        String input = "4\n0\n";
        Book book1 = new Book("Titre1", "Auteur1", LocalDate.now(), "ISBN1", "Cat1");
        Book book2 = new Book("Titre2", "Auteur2", LocalDate.now(), "ISBN2", "Cat2");
        List<Book> books = Arrays.asList(book1, book2);

        when(bookManager.listerTousLesLivres()).thenReturn(books);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager, atLeastOnce()).listerTousLesLivres();
        String output = outputStream.toString();
        assertTrue(output.contains("TOUS LES LIVRES"));
    }

    @Test
    void testAfficherMenu_ChoixListerLivresDisponibles_ShouldCallManager() {
        // Arrange
        String input = "5\n0\n";
        Book book = new Book("Livre Disponible", "Auteur", LocalDate.now(), "ISBN", "Cat");
        List<Book> books = List.of(book);

        when(bookManager.listerLivresDisponibles()).thenReturn(books);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).listerLivresDisponibles();
        String output = outputStream.toString();
        assertTrue(output.contains("LIVRES DISPONIBLES"));
    }

    @Test
    void testAfficherMenu_ChoixListerLivresEmpruntes_ShouldCallManager() {
        // Arrange
        String input = "6\n0\n";
        Book book = new Book("Livre Emprunté", "Auteur", LocalDate.now(), "ISBN", "Cat");
        List<Book> books = List.of(book);

        when(bookManager.listerLivresEmpruntes()).thenReturn(books);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).listerLivresEmpruntes();
        String output = outputStream.toString();
        assertTrue(output.contains("LIVRES EMPRUNTÉS"));
    }

    @Test
    void testAfficherMenu_RechercherParTitre_ShouldCallManager() {
        // Arrange
        String input = "7\nTitre Recherché\n0\n";
        Book book = new Book("Titre Recherché", "Auteur", LocalDate.now(), "ISBN", "Cat");
        List<Book> books = List.of(book);

        when(bookManager.rechercherParTitre("Titre Recherché")).thenReturn(books);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).rechercherParTitre("Titre Recherché");
        String output = outputStream.toString();
        assertTrue(output.contains("RECHERCHE PAR TITRE"));
    }

    @Test
    void testAfficherMenu_RechercherParTitre_AucunResultat_ShouldShowMessage() {
        // Arrange
        String input = "7\nTitre Inexistant\n0\n";

        when(bookManager.rechercherParTitre("Titre Inexistant")).thenReturn(List.of());
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).rechercherParTitre("Titre Inexistant");
        String output = outputStream.toString();
        assertTrue(output.contains("Aucun livre trouvé"));
    }

    @Test
    void testAfficherMenu_RechercherParAuteur_ShouldCallManager() {
        // Arrange
        String input = "8\nAuteur Test\n0\n";
        Book book = new Book("Titre", "Auteur Test", LocalDate.now(), "ISBN", "Cat");
        List<Book> books = List.of(book);

        when(bookManager.rechercherParAuteur("Auteur Test")).thenReturn(books);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).rechercherParAuteur("Auteur Test");
        String output = outputStream.toString();
        assertTrue(output.contains("RECHERCHE PAR AUTEUR"));
    }

    @Test
    void testAfficherMenu_RechercherParCategorie_ShouldCallManager() {
        // Arrange
        String input = "9\nFantasy\n0\n";
        Book book = new Book("Titre", "Auteur", LocalDate.now(), "ISBN", "Fantasy");
        List<Book> books = List.of(book);

        when(bookManager.rechercherParCategorie("Fantasy")).thenReturn(books);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).rechercherParCategorie("Fantasy");
        String output = outputStream.toString();
        assertTrue(output.contains("RECHERCHE PAR CATÉGORIE"));
    }

    @Test
    void testAfficherMenu_AjouterLivre_Success_ShouldCallManager() {
        // Arrange
        String input = "1\nLe Petit Prince\nAntoine de Saint-Exupéry\n06/04/1943\n978-2-07-061275-8\nConte\n0\n";
        Book newBook = new Book("Le Petit Prince", "Antoine de Saint-Exupéry",
                LocalDate.of(1943, 4, 6), "978-2-07-061275-8", "Conte");
        newBook.setId(1L);

        when(bookManager.isbnExiste("978-2-07-061275-8")).thenReturn(false);
        when(bookManager.ajouterLivre(anyString(), anyString(), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(newBook);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).isbnExiste("978-2-07-061275-8");
        verify(bookManager).ajouterLivre(eq("Le Petit Prince"), eq("Antoine de Saint-Exupéry"),
                any(LocalDate.class), eq("978-2-07-061275-8"), eq("Conte"));
        String output = outputStream.toString();
        assertTrue(output.contains("AJOUTER UN LIVRE"));
        assertTrue(output.contains("Livre ajouté avec succès"));
    }

    @Test
    void testAfficherMenu_AjouterLivre_ISBNExistant_ShouldShowError() {
        // Arrange
        String input = "1\nTitre\nAuteur\n01/01/2020\n978-EXISTING\nCat\n0\n";

        when(bookManager.isbnExiste("978-EXISTING")).thenReturn(true);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).isbnExiste("978-EXISTING");
        verify(bookManager, never()).ajouterLivre(anyString(), anyString(), any(), anyString(), anyString());
        String output = outputStream.toString();
        assertTrue(output.contains("Un livre avec cet ISBN existe déjà"));
    }

    @Test
    void testAfficherMenu_ModifierLivre_Success_ShouldCallManager() {
        // Arrange
        Book existingBook = new Book("Ancien Titre", "Ancien Auteur",
                LocalDate.of(2020, 1, 1), "ISBN123", "Cat");
        existingBook.setId(1L);

        Book updatedBook = new Book("Nouveau Titre", "Nouvel Auteur",
                LocalDate.of(2021, 1, 1), "ISBN123", "Nouvelle Cat");
        updatedBook.setId(1L);

        String input = "2\n1\nNouveau Titre\nNouvel Auteur\n01/01/2021\nISBN123\nNouvelle Cat\n0\n";

        when(bookManager.listerTousLesLivres()).thenReturn(List.of(existingBook));
        when(bookManager.trouverParId(1L)).thenReturn(Optional.of(existingBook));
        when(bookManager.modifierLivre(eq(1L), anyString(), anyString(), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(updatedBook);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).trouverParId(1L);
        verify(bookManager).modifierLivre(eq(1L), eq("Nouveau Titre"), eq("Nouvel Auteur"),
                any(LocalDate.class), eq("ISBN123"), eq("Nouvelle Cat"));
        String output = outputStream.toString();
        assertTrue(output.contains("MODIFIER UN LIVRE"));
        assertTrue(output.contains("Livre modifié avec succès"));
    }

    @Test
    void testAfficherMenu_ModifierLivre_LivreNonTrouve_ShouldShowError() {
        // Arrange
        String input = "2\n999\n0\n";

        when(bookManager.listerTousLesLivres()).thenReturn(List.of());
        when(bookManager.trouverParId(999L)).thenReturn(Optional.empty());
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).trouverParId(999L);
        verify(bookManager, never()).modifierLivre(anyLong(), anyString(), anyString(), any(), anyString(), anyString());
        String output = outputStream.toString();
        assertTrue(output.contains("Livre non trouvé"));
    }

    @Test
    void testAfficherMenu_SupprimerLivre_Confirme_ShouldCallManager() {
        // Arrange
        Book bookToDelete = new Book("Livre à supprimer", "Auteur",
                LocalDate.now(), "ISBN", "Cat");
        bookToDelete.setId(1L);

        String input = "3\n1\noui\n0\n";

        when(bookManager.listerTousLesLivres()).thenReturn(List.of(bookToDelete));
        when(bookManager.trouverParId(1L)).thenReturn(Optional.of(bookToDelete));
        doNothing().when(bookManager).supprimerLivre(1L);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).trouverParId(1L);
        verify(bookManager).supprimerLivre(1L);
        String output = outputStream.toString();
        assertTrue(output.contains("SUPPRIMER UN LIVRE"));
        assertTrue(output.contains("Livre supprimé avec succès"));
    }

    @Test
    void testAfficherMenu_SupprimerLivre_Annule_ShouldNotCallManager() {
        // Arrange
        Book bookToDelete = new Book("Livre", "Auteur", LocalDate.now(), "ISBN", "Cat");
        bookToDelete.setId(1L);

        String input = "3\n1\nnon\n0\n";

        when(bookManager.listerTousLesLivres()).thenReturn(List.of(bookToDelete));
        when(bookManager.trouverParId(1L)).thenReturn(Optional.of(bookToDelete));
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(bookManager).trouverParId(1L);
        verify(bookManager, never()).supprimerLivre(anyLong());
        String output = outputStream.toString();
        assertTrue(output.contains("Suppression annulée"));
    }

    @Test
    void testAfficherMenu_EmprunterLivre_Success_ShouldCallManager() {
        // Arrange
        Book book = new Book("Livre", "Auteur", LocalDate.now(), "ISBN", "Cat");
        book.setId(1L);
        User user = new User("Dupont", "Jean", "jean@example.com", "pass");
        user.setId(1L);

        String input = "10\n1\n1\n0\n";

        when(bookManager.listerLivresDisponibles()).thenReturn(List.of(book));
        when(userManager.listerTousLesUtilisateurs()).thenReturn(List.of(user));
        when(bookManager.trouverParId(1L)).thenReturn(Optional.of(book));
        when(userManager.trouverParId(1L)).thenReturn(Optional.of(user));
        when(empruntManager.emprunterLivre(any(User.class), any(Book.class))).thenReturn(null);
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(empruntManager).emprunterLivre(any(User.class), any(Book.class));
        String output = outputStream.toString();
        assertTrue(output.contains("EMPRUNTER UN LIVRE"));
    }

    @Test
    void testAfficherMenu_RendreLivre_Success_ShouldCallManager() {
        // Arrange
        Book book = new Book("Livre", "Auteur", LocalDate.now(), "ISBN", "Cat");
        book.setId(1L);
        User user = new User("Dupont", "Jean", "jean@example.com", "pass");
        user.setId(1L);
        Emprunt emprunt = new Emprunt(user, book, LocalDate.now());
        emprunt.setId(1L);

        // Ajouter l'emprunt au livre pour que isEmprunte() retourne true
        book.addEmprunt(emprunt);

        String input = "11\n1\noui\n0\n";

        when(bookManager.listerLivresEmpruntes()).thenReturn(List.of(book));
        when(bookManager.trouverParId(1L)).thenReturn(Optional.of(book));
        when(empruntManager.getEmpruntEnCours(book)).thenReturn(Optional.of(emprunt));
        doNothing().when(empruntManager).rendreLivre(any(Book.class));
        bookDisplay.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));

        // Act
        bookDisplay.afficherMenu();

        // Assert
        verify(empruntManager).rendreLivre(any(Book.class));
        String output = outputStream.toString();
        assertTrue(output.contains("RENDRE UN LIVRE"));
    }

    @Test
    void testSetScanner_ShouldSetNewScanner() {
        // Arrange
        Scanner newScanner = new Scanner("test");

        // Act
        bookDisplay.setScanner(newScanner);

        // Assert - pas d'exception levée
        assertNotNull(bookDisplay);
    }
}
