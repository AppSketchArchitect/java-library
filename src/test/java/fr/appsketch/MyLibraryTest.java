package fr.appsketch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MyLibrary
 * Note: Cette classe contient principalement des méthodes statiques et l'interaction avec System.in/out,
 * donc les tests se concentrent sur la vérification de la structure et de l'existence de la classe.
 */
class MyLibraryTest {

    @Test
    void testMyLibraryClassExists() {
        // Assert - Vérifier que la classe existe et peut être instanciée
        assertNotNull(MyLibrary.class);
    }

    @Test
    void testMyLibraryHasMainMethod() throws NoSuchMethodException {
        // Arrange & Act
        var mainMethod = MyLibrary.class.getDeclaredMethod("main", String[].class);

        // Assert
        assertNotNull(mainMethod);
        assertEquals(void.class, mainMethod.getReturnType());
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()));
    }

    @Test
    void testMyLibraryHasInitialiserApplicationMethod() throws NoSuchMethodException {
        // Arrange & Act
        var method = MyLibrary.class.getDeclaredMethod("initialiserApplication");

        // Assert
        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(method.getModifiers()));
    }

    @Test
    void testMyLibraryHasAfficherMenuPrincipalMethod() throws NoSuchMethodException {
        // Arrange & Act
        var method = MyLibrary.class.getDeclaredMethod("afficherMenuPrincipal");

        // Assert
        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(method.getModifiers()));
    }

    @Test
    void testMyLibraryHasAfficherMenuLivresMethod() throws NoSuchMethodException {
        // Arrange & Act
        var method = MyLibrary.class.getDeclaredMethod("afficherMenuLivres");

        // Assert
        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(method.getModifiers()));
    }

    @Test
    void testMyLibraryHasAfficherMenuUtilisateursMethod() throws NoSuchMethodException {
        // Arrange & Act
        var method = MyLibrary.class.getDeclaredMethod("afficherMenuUtilisateurs");

        // Assert
        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(method.getModifiers()));
    }

    @Test
    void testMyLibraryClassName() {
        // Assert
        assertEquals("MyLibrary", MyLibrary.class.getSimpleName());
        assertEquals("fr.appsketch.MyLibrary", MyLibrary.class.getName());
    }

    @Test
    void testMyLibraryPackage() {
        // Assert
        assertEquals("fr.appsketch", MyLibrary.class.getPackageName());
    }

    @Test
    void testMyLibraryIsPublicClass() {
        // Assert
        assertTrue(java.lang.reflect.Modifier.isPublic(MyLibrary.class.getModifiers()));
    }

    @Test
    void testMyLibraryHasStaticFields() throws NoSuchFieldException {
        // Vérifier l'existence des champs statiques
        var scannerField = MyLibrary.class.getDeclaredField("scanner");
        var emField = MyLibrary.class.getDeclaredField("em");
        var bookManagerField = MyLibrary.class.getDeclaredField("bookManager");
        var userManagerField = MyLibrary.class.getDeclaredField("userManager");
        var empruntManagerField = MyLibrary.class.getDeclaredField("empruntManager");

        // Assert
        assertNotNull(scannerField);
        assertTrue(java.lang.reflect.Modifier.isStatic(scannerField.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(scannerField.getModifiers()));

        assertNotNull(emField);
        assertTrue(java.lang.reflect.Modifier.isStatic(emField.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(emField.getModifiers()));

        assertNotNull(bookManagerField);
        assertTrue(java.lang.reflect.Modifier.isStatic(bookManagerField.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(bookManagerField.getModifiers()));

        assertNotNull(userManagerField);
        assertTrue(java.lang.reflect.Modifier.isStatic(userManagerField.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(userManagerField.getModifiers()));

        assertNotNull(empruntManagerField);
        assertTrue(java.lang.reflect.Modifier.isStatic(empruntManagerField.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPrivate(empruntManagerField.getModifiers()));
    }

    @Test
    void testMyLibraryFieldTypes() throws NoSuchFieldException {
        // Vérifier les types des champs
        var scannerField = MyLibrary.class.getDeclaredField("scanner");
        var emField = MyLibrary.class.getDeclaredField("em");
        var bookManagerField = MyLibrary.class.getDeclaredField("bookManager");
        var userManagerField = MyLibrary.class.getDeclaredField("userManager");
        var empruntManagerField = MyLibrary.class.getDeclaredField("empruntManager");

        // Assert
        assertEquals(java.util.Scanner.class, scannerField.getType());
        assertEquals(jakarta.persistence.EntityManager.class, emField.getType());
        assertEquals(fr.appsketch.Book.BookManager.class, bookManagerField.getType());
        assertEquals(fr.appsketch.User.UserManager.class, userManagerField.getType());
        assertEquals(fr.appsketch.Emprunt.EmpruntManager.class, empruntManagerField.getType());
    }

    @Test
    void testMyLibraryCanBeInstantiated() {
        // Arrange & Act
        MyLibrary library = new MyLibrary();

        // Assert
        assertNotNull(library);
        assertTrue(library instanceof MyLibrary);
    }

    @Test
    void testMainMethodSignature() throws NoSuchMethodException {
        // Arrange & Act
        var mainMethod = MyLibrary.class.getDeclaredMethod("main", String[].class);
        var parameterTypes = mainMethod.getParameterTypes();

        // Assert
        assertEquals(1, parameterTypes.length);
        assertEquals(String[].class, parameterTypes[0]);
    }

    @Test
    void testAllPrivateMethodsExist() {
        // Arrange
        var methods = MyLibrary.class.getDeclaredMethods();
        long privateMethodsCount = java.util.Arrays.stream(methods)
                .filter(m -> java.lang.reflect.Modifier.isPrivate(m.getModifiers()))
                .count();

        // Assert - Devrait avoir 4 méthodes privées
        assertTrue(privateMethodsCount >= 4,
                "Should have at least 4 private methods (initialiserApplication, afficherMenuPrincipal, afficherMenuLivres, afficherMenuUtilisateurs)");
    }

    @Test
    void testMyLibraryHasExpectedMethods() {
        // Arrange
        var methods = MyLibrary.class.getDeclaredMethods();
        var methodNames = java.util.Arrays.stream(methods)
                .map(java.lang.reflect.Method::getName)
                .collect(java.util.stream.Collectors.toSet());

        // Assert
        assertTrue(methodNames.contains("main"));
        assertTrue(methodNames.contains("initialiserApplication"));
        assertTrue(methodNames.contains("afficherMenuPrincipal"));
        assertTrue(methodNames.contains("afficherMenuLivres"));
        assertTrue(methodNames.contains("afficherMenuUtilisateurs"));
    }
}
