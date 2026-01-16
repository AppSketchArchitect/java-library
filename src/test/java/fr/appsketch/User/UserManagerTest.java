package fr.appsketch.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests unitaires pour UserManager
 * Utilise JUnit 5 et Mockito pour mocker les dépendances
 */
@ExtendWith(MockitoExtension.class)
class UserManagerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction transaction;

    private UserManager userManager;

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

        userManager = new UserManager(userRepository, entityManager);
    }

    @Test
    void testAjouterUtilisateur_Success() {
        // Arrange
        String nom = "Dupont";
        String prenom = "Jean";
        String email = "jean.dupont@example.com";
        String motDePasse = "password123";

        User expectedUser = new User(nom, prenom, email, motDePasse);
        expectedUser.setId(1L);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Act
        User result = userManager.ajouterUtilisateur(nom, prenom, email, motDePasse);

        // Assert
        assertNotNull(result);
        assertEquals(nom, result.getNom());
        assertEquals(prenom, result.getPrenom());
        assertEquals(email, result.getEmail());
        verify(transaction).begin();
        verify(transaction).commit();
        verify(userRepository).save(any(User.class));
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testAjouterUtilisateur_NomNull_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.ajouterUtilisateur(null, "Prenom", "email@test.com", "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAjouterUtilisateur_NomVide_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.ajouterUtilisateur("   ", "Prenom", "email@test.com", "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAjouterUtilisateur_PrenomNull_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.ajouterUtilisateur("Nom", null, "email@test.com", "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAjouterUtilisateur_EmailNull_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.ajouterUtilisateur("Nom", "Prenom", null, "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAjouterUtilisateur_MotDePasseNull_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.ajouterUtilisateur("Nom", "Prenom", "email@test.com", null);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAjouterUtilisateur_EmailExisteDeja_ThrowsException() {
        // Arrange
        String email = "existing@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.ajouterUtilisateur("Nom", "Prenom", email, "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAjouterUtilisateur_ExceptionLorsDeLaSauvegarde_Rollback() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Erreur DB"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userManager.ajouterUtilisateur("Nom", "Prenom", "email@test.com", "password");
        });

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    void testModifierUtilisateur_Success() {
        // Arrange
        Long id = 1L;
        User existingUser = new User("Ancien Nom", "Ancien Prenom", "ancien@test.com", "oldpass");
        existingUser.setId(id);

        User updatedUser = new User("Nouveau Nom", "Nouveau Prenom", "nouveau@test.com", "newpass");
        updatedUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userRepository.existsByEmail("nouveau@test.com")).thenReturn(false);

        // Act
        User result = userManager.modifierUtilisateur(id, "Nouveau Nom", "Nouveau Prenom",
                "nouveau@test.com", "newpass");

        // Assert
        assertNotNull(result);
        verify(transaction).begin();
        verify(transaction).commit();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testModifierUtilisateur_UtilisateurNonTrouve_ThrowsException() {
        // Arrange
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.modifierUtilisateur(id, "Nom", "Prenom", "email@test.com", "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testModifierUtilisateur_EmailDejaUtilise_ThrowsException() {
        // Arrange
        Long id = 1L;
        User existingUser = new User("Nom", "Prenom", "ancien@test.com", "password");
        existingUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("nouveau@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.modifierUtilisateur(id, "Nom", "Prenom", "nouveau@test.com", "password");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testModifierUtilisateur_MemeEmail_Success() {
        // Arrange
        Long id = 1L;
        String email = "same@test.com";
        User existingUser = new User("Nom", "Prenom", email, "password");
        existingUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        User result = userManager.modifierUtilisateur(id, "Nouveau Nom", "Nouveau Prenom", email, "newpass");

        // Assert
        assertNotNull(result);
        verify(transaction).begin();
        verify(transaction).commit();
        verify(userRepository).save(any(User.class));
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void testSupprimerUtilisateur_Success() {
        // Arrange
        Long id = 1L;
        User user = new User("Nom", "Prenom", "email@test.com", "password");
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act
        userManager.supprimerUtilisateur(id);

        // Assert
        verify(transaction).begin();
        verify(transaction).commit();
        verify(userRepository).deleteById(id);
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void testSupprimerUtilisateur_UtilisateurNonTrouve_ThrowsException() {
        // Arrange
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.supprimerUtilisateur(id);
        });

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testTrouverParId_UtilisateurTrouve() {
        // Arrange
        Long id = 1L;
        User user = new User("Nom", "Prenom", "email@test.com", "password");
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userManager.trouverParId(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(userRepository).findById(id);
    }

    @Test
    void testTrouverParId_UtilisateurNonTrouve() {
        // Arrange
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userManager.trouverParId(id);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(id);
    }

    @Test
    void testListerTousLesUtilisateurs() {
        // Arrange
        List<User> users = Arrays.asList(
                new User("Dupont", "Jean", "jean@test.com", "pass1"),
                new User("Martin", "Marie", "marie@test.com", "pass2")
        );

        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userManager.listerTousLesUtilisateurs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testRechercherParEmail_EmailTrouve() {
        // Arrange
        String email = "test@example.com";
        User user = new User("Nom", "Prenom", email, "password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userManager.rechercherParEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testRechercherParEmail_EmailNonTrouve() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userManager.rechercherParEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testRechercherParEmail_EmailNull() {
        // Act
        Optional<User> result = userManager.rechercherParEmail(null);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testRechercherParEmail_EmailVide() {
        // Act
        Optional<User> result = userManager.rechercherParEmail("   ");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testRechercherParNom_NomValide() {
        // Arrange
        User user1 = new User("Dupont", "Jean", "jean@test.com", "pass1");
        User user2 = new User("Martin", "Marie", "marie@test.com", "pass2");
        User user3 = new User("Durand", "Pierre", "pierre@test.com", "pass3");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // Act
        List<User> result = userManager.rechercherParNom("Du");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> u.getNom().toLowerCase().contains("du")));
    }

    @Test
    void testRechercherParNom_NomNull() {
        // Act
        List<User> result = userManager.rechercherParNom(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findAll();
    }

    @Test
    void testRechercherParPrenom_PrenomValide() {
        // Arrange
        User user1 = new User("Dupont", "Jean", "jean@test.com", "pass1");
        User user2 = new User("Martin", "Marie", "marie@test.com", "pass2");
        User user3 = new User("Durand", "Jeanne", "jeanne@test.com", "pass3");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // Act
        List<User> result = userManager.rechercherParPrenom("Jean");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> u.getPrenom().toLowerCase().contains("jean")));
    }

    @Test
    void testRechercherParPrenom_PrenomNull() {
        // Act
        List<User> result = userManager.rechercherParPrenom(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findAll();
    }

    @Test
    void testEmailExiste_EmailExiste() {
        // Arrange
        String email = "existing@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = userManager.emailExiste(email);

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void testEmailExiste_EmailNexistePas() {
        // Arrange
        String email = "nonexistent@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean result = userManager.emailExiste(email);

        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void testEmailExiste_EmailNull() {
        // Act
        boolean result = userManager.emailExiste(null);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void testEmailExiste_EmailVide() {
        // Act
        boolean result = userManager.emailExiste("");

        // Assert
        assertFalse(result);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void testListerTousLesUtilisateurs_ListeVide() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<User> result = userManager.listerTousLesUtilisateurs();

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void testRechercherParNom_AucunResultat() {
        // Arrange
        User user1 = new User("Dupont", "Jean", "jean@test.com", "pass1");
        when(userRepository.findAll()).thenReturn(List.of(user1));

        // Act
        List<User> result = userManager.rechercherParNom("Inexistant");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testRechercherParPrenom_AucunResultat() {
        // Arrange
        User user1 = new User("Dupont", "Jean", "jean@test.com", "pass1");
        when(userRepository.findAll()).thenReturn(List.of(user1));

        // Act
        List<User> result = userManager.rechercherParPrenom("Inexistant");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testRechercherParNom_NomVide() {
        // Act
        List<User> result = userManager.rechercherParNom("");

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findAll();
    }

    @Test
    void testRechercherParPrenom_PrenomVide() {
        // Act
        List<User> result = userManager.rechercherParPrenom("");

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findAll();
    }
}
