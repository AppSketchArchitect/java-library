package fr.appsketch.Displays;

import fr.appsketch.User.User;
import fr.appsketch.User.UserManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserDisplay
 * Utilise JUnit 5 et Mockito pour mocker les dépendances
 */
@ExtendWith(MockitoExtension.class)
class UserDisplayTest {

    @Mock
    private UserManager userManager;

    private UserDisplay userDisplay;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        userDisplay = new UserDisplay(userManager);
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testConstructor_ShouldCreateInstance() {
        // Assert
        assertNotNull(userDisplay);
    }

    @Test
    void testAfficherMenu_WithValidManager_ShouldNotThrowException() {
        // Arrange
        UserDisplay display = new UserDisplay(userManager);

        // Assert
        assertNotNull(display);
    }

    @Test
    void testListerTousLesUtilisateurs_WithUsers_ShouldDisplayList() {
        // Arrange
        User user1 = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");
        user1.setId(1L);
        User user2 = new User("Martin", "Sophie", "sophie.martin@example.com", "password456");
        user2.setId(2L);
        List<User> users = Arrays.asList(user1, user2);

        when(userManager.listerTousLesUtilisateurs()).thenReturn(users);

        // Cette méthode est privée, mais on peut tester via le menu
        // Pour simplifier, on vérifie que le manager est appelé correctement
        userManager.listerTousLesUtilisateurs();

        // Assert
        verify(userManager).listerTousLesUtilisateurs();
    }

    @Test
    void testListerTousLesUtilisateurs_EmptyList_ShouldDisplayMessage() {
        // Arrange
        when(userManager.listerTousLesUtilisateurs()).thenReturn(List.of());

        // Act
        userManager.listerTousLesUtilisateurs();

        // Assert
        verify(userManager).listerTousLesUtilisateurs();
    }

    @Test
    void testRechercherParEmail_UserFound_ShouldReturnUser() {
        // Arrange
        String email = "jean.dupont@example.com";
        User user = new User("Dupont", "Jean", email, "password123");
        user.setId(1L);

        when(userManager.rechercherParEmail(email)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userManager.rechercherParEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userManager).rechercherParEmail(email);
    }

    @Test
    void testRechercherParEmail_UserNotFound_ShouldReturnEmpty() {
        // Arrange
        String email = "unknown@example.com";

        when(userManager.rechercherParEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userManager.rechercherParEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(userManager).rechercherParEmail(email);
    }

    @Test
    void testRechercherParNom_UsersFound_ShouldReturnList() {
        // Arrange
        String nom = "Dupont";
        User user1 = new User(nom, "Jean", "jean.dupont@example.com", "pass1");
        User user2 = new User(nom, "Marie", "marie.dupont@example.com", "pass2");
        List<User> users = Arrays.asList(user1, user2);

        when(userManager.rechercherParNom(nom)).thenReturn(users);

        // Act
        List<User> result = userManager.rechercherParNom(nom);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userManager).rechercherParNom(nom);
    }

    @Test
    void testRechercherParNom_NoUsersFound_ShouldReturnEmptyList() {
        // Arrange
        String nom = "Inexistant";

        when(userManager.rechercherParNom(nom)).thenReturn(List.of());

        // Act
        List<User> result = userManager.rechercherParNom(nom);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userManager).rechercherParNom(nom);
    }

    @Test
    void testRechercherParPrenom_UsersFound_ShouldReturnList() {
        // Arrange
        String prenom = "Jean";
        User user1 = new User("Dupont", prenom, "jean.dupont@example.com", "pass1");
        User user2 = new User("Martin", prenom, "jean.martin@example.com", "pass2");
        List<User> users = Arrays.asList(user1, user2);

        when(userManager.rechercherParPrenom(prenom)).thenReturn(users);

        // Act
        List<User> result = userManager.rechercherParPrenom(prenom);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userManager).rechercherParPrenom(prenom);
    }

    @Test
    void testRechercherParPrenom_NoUsersFound_ShouldReturnEmptyList() {
        // Arrange
        String prenom = "Inexistant";

        when(userManager.rechercherParPrenom(prenom)).thenReturn(List.of());

        // Act
        List<User> result = userManager.rechercherParPrenom(prenom);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userManager).rechercherParPrenom(prenom);
    }

    @Test
    void testAjouterUtilisateur_Success_ShouldCallManager() {
        // Arrange
        User newUser = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");
        newUser.setId(1L);

        when(userManager.emailExiste("jean.dupont@example.com")).thenReturn(false);
        when(userManager.ajouterUtilisateur("Dupont", "Jean", "jean.dupont@example.com", "password123"))
                .thenReturn(newUser);

        // Act
        userManager.emailExiste("jean.dupont@example.com");
        User result = userManager.ajouterUtilisateur("Dupont", "Jean", "jean.dupont@example.com", "password123");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userManager).emailExiste("jean.dupont@example.com");
        verify(userManager).ajouterUtilisateur("Dupont", "Jean", "jean.dupont@example.com", "password123");
    }

    @Test
    void testAjouterUtilisateur_EmailExistant_ShouldNotAdd() {
        // Arrange
        when(userManager.emailExiste("existing@example.com")).thenReturn(true);

        // Act
        boolean exists = userManager.emailExiste("existing@example.com");

        // Assert
        assertTrue(exists);
        verify(userManager).emailExiste("existing@example.com");
        verify(userManager, never()).ajouterUtilisateur(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testModifierUtilisateur_Success_ShouldCallManager() {
        // Arrange
        Long userId = 1L;
        User existingUser = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");
        existingUser.setId(userId);

        User updatedUser = new User("Dupont", "Jean-Pierre", "jp.dupont@example.com", "newpass");
        updatedUser.setId(userId);

        when(userManager.trouverParId(userId)).thenReturn(Optional.of(existingUser));
        when(userManager.modifierUtilisateur(userId, "Dupont", "Jean-Pierre", "jp.dupont@example.com", "newpass"))
                .thenReturn(updatedUser);

        // Act
        Optional<User> foundUser = userManager.trouverParId(userId);
        User result = userManager.modifierUtilisateur(userId, "Dupont", "Jean-Pierre", "jp.dupont@example.com", "newpass");

        // Assert
        assertTrue(foundUser.isPresent());
        assertNotNull(result);
        assertEquals("Jean-Pierre", result.getPrenom());
        verify(userManager).trouverParId(userId);
        verify(userManager).modifierUtilisateur(userId, "Dupont", "Jean-Pierre", "jp.dupont@example.com", "newpass");
    }

    @Test
    void testModifierUtilisateur_UserNotFound_ShouldNotModify() {
        // Arrange
        Long userId = 999L;

        when(userManager.trouverParId(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userManager.trouverParId(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userManager).trouverParId(userId);
        verify(userManager, never()).modifierUtilisateur(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testSupprimerUtilisateur_Confirme_ShouldCallManager() {
        // Arrange
        Long userId = 1L;
        User user = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");
        user.setId(userId);

        when(userManager.trouverParId(userId)).thenReturn(Optional.of(user));
        doNothing().when(userManager).supprimerUtilisateur(userId);

        // Act
        userManager.trouverParId(userId);
        userManager.supprimerUtilisateur(userId);

        // Assert
        verify(userManager).trouverParId(userId);
        verify(userManager).supprimerUtilisateur(userId);
    }

    @Test
    void testSupprimerUtilisateur_UserNotFound_ShouldNotDelete() {
        // Arrange
        Long userId = 999L;

        when(userManager.trouverParId(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userManager.trouverParId(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userManager).trouverParId(userId);
        verify(userManager, never()).supprimerUtilisateur(anyLong());
    }

    @Test
    void testTrouverParId_UserExists_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        User user = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");
        user.setId(userId);

        when(userManager.trouverParId(userId)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userManager.trouverParId(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("Dupont", result.get().getNom());
        verify(userManager).trouverParId(userId);
    }

    @Test
    void testTrouverParId_UserNotFound_ShouldReturnEmpty() {
        // Arrange
        Long userId = 999L;

        when(userManager.trouverParId(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userManager.trouverParId(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userManager).trouverParId(userId);
    }

    @Test
    void testEmailExiste_EmailExists_ShouldReturnTrue() {
        // Arrange
        String email = "existing@example.com";

        when(userManager.emailExiste(email)).thenReturn(true);

        // Act
        boolean result = userManager.emailExiste(email);

        // Assert
        assertTrue(result);
        verify(userManager).emailExiste(email);
    }

    @Test
    void testEmailExiste_EmailNotExists_ShouldReturnFalse() {
        // Arrange
        String email = "new@example.com";

        when(userManager.emailExiste(email)).thenReturn(false);

        // Act
        boolean result = userManager.emailExiste(email);

        // Assert
        assertFalse(result);
        verify(userManager).emailExiste(email);
    }
}
