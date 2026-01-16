package fr.appsketch.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserRepository
 * Utilise JUnit 5 et Mockito pour mocker EntityManager
 */
@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<User> typedQuery;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository(entityManager);
    }

    @Test
    void testSave_NewUser_ShouldPersist() {
        // Arrange
        User newUser = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");

        // Act
        User result = userRepository.save(newUser);

        // Assert
        assertNotNull(result);
        verify(entityManager).persist(newUser);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testSave_ExistingUser_ShouldMerge() {
        // Arrange
        User existingUser = new User("Martin", "Sophie", "sophie.martin@example.com", "securepass");
        existingUser.setId(1L);

        when(entityManager.merge(existingUser)).thenReturn(existingUser);

        // Act
        User result = userRepository.save(existingUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(entityManager).merge(existingUser);
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testFindById_UserExists_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        User expectedUser = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");
        expectedUser.setId(userId);

        when(entityManager.find(User.class, userId)).thenReturn(expectedUser);

        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("Dupont", result.get().getNom());
        assertEquals("Jean", result.get().getPrenom());
        verify(entityManager).find(User.class, userId);
    }

    @Test
    void testFindById_UserNotFound_ShouldReturnEmpty() {
        // Arrange
        Long userId = 999L;
        when(entityManager.find(User.class, userId)).thenReturn(null);

        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(entityManager).find(User.class, userId);
    }

    @Test
    void testFindAll_ShouldReturnAllUsers() {
        // Arrange
        User user1 = new User("Dupont", "Jean", "jean.dupont@example.com", "pass1");
        User user2 = new User("Martin", "Sophie", "sophie.martin@example.com", "pass2");
        List<User> expectedUsers = Arrays.asList(user1, user2);

        when(entityManager.createQuery("SELECT u FROM User u", User.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedUsers);

        // Act
        List<User> result = userRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(entityManager).createQuery("SELECT u FROM User u", User.class);
        verify(typedQuery).getResultList();
    }

    @Test
    void testFindAll_EmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(entityManager.createQuery("SELECT u FROM User u", User.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        // Act
        List<User> result = userRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).createQuery("SELECT u FROM User u", User.class);
    }

    @Test
    void testFindByEmail_UserExists_ShouldReturnUser() {
        // Arrange
        String email = "jean.dupont@example.com";
        User expectedUser = new User("Dupont", "Jean", email, "password123");

        when(entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("email", email)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(expectedUser));

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        assertEquals("Dupont", result.get().getNom());
        verify(typedQuery).setParameter("email", email);
    }

    @Test
    void testFindByEmail_UserNotFound_ShouldReturnEmpty() {
        // Arrange
        String email = "unknown@example.com";

        when(entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("email", email)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(typedQuery).setParameter("email", email);
    }

    @Test
    void testExistsByEmail_UserExists_ShouldReturnTrue() {
        // Arrange
        String email = "jean.dupont@example.com";

        when(entityManager.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter("email", email)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(1L);

        // Act
        boolean result = userRepository.existsByEmail(email);

        // Assert
        assertTrue(result);
        verify(longTypedQuery).setParameter("email", email);
        verify(longTypedQuery).getSingleResult();
    }

    @Test
    void testExistsByEmail_UserNotFound_ShouldReturnFalse() {
        // Arrange
        String email = "unknown@example.com";

        when(entityManager.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.setParameter("email", email)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(0L);

        // Act
        boolean result = userRepository.existsByEmail(email);

        // Assert
        assertFalse(result);
        verify(longTypedQuery).setParameter("email", email);
    }

    @Test
    void testDeleteById_UserExists_ShouldRemove() {
        // Arrange
        Long userId = 1L;
        User userToDelete = new User("Dupont", "Jean", "jean.dupont@example.com", "password123");
        userToDelete.setId(userId);

        when(entityManager.find(User.class, userId)).thenReturn(userToDelete);

        // Act
        userRepository.deleteById(userId);

        // Assert
        verify(entityManager).find(User.class, userId);
        verify(entityManager).remove(userToDelete);
    }

    @Test
    void testDeleteById_UserNotFound_ShouldNotRemove() {
        // Arrange
        Long userId = 999L;
        when(entityManager.find(User.class, userId)).thenReturn(null);

        // Act
        userRepository.deleteById(userId);

        // Assert
        verify(entityManager).find(User.class, userId);
        verify(entityManager, never()).remove(any());
    }
}
