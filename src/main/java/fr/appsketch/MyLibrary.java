package fr.appsketch;

import fr.appsketch.Book.BookManager;
import fr.appsketch.Book.BookRepository;
import fr.appsketch.Core.HibernateManager;
import fr.appsketch.Emprunt.EmpruntManager;
import fr.appsketch.Emprunt.EmpruntRepository;
import fr.appsketch.User.UserManager;
import fr.appsketch.User.UserRepository;
import jakarta.persistence.EntityManager;

import java.util.Scanner;

/**
 * Classe principale de l'application de gestion de bibliothèque
 * Point d'entrée unique pour accéder aux différents modules
 */
public class MyLibrary {

    private static Scanner scanner;
    private static EntityManager em;
    private static BookManager bookManager;
    private static UserManager userManager;
    private static EmpruntManager empruntManager;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Initialisation d'Hibernate et des managers
        initialiserApplication();

        try {
            afficherMenuPrincipal();
        } finally {
            // Nettoyage des ressources
            scanner.close();
            em.close();
            HibernateManager.shutdown();
            System.out.println("\n👋 Application fermée. À bientôt !");
        }
    }

    private static void initialiserApplication() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║     📚 SYSTÈME DE GESTION DE BIBLIOTHÈQUE     ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println("\n⏳ Initialisation en cours...");

        // Création de l'EntityManager
        em = HibernateManager.getSessionFactory().createEntityManager();

        // Initialisation des repositories
        BookRepository bookRepository = new BookRepository(em);
        UserRepository userRepository = new UserRepository(em);
        EmpruntRepository empruntRepository = new EmpruntRepository(em);

        // Initialisation des managers
        bookManager = new BookManager(bookRepository, em);
        userManager = new UserManager(userRepository, em);
        empruntManager = new EmpruntManager(empruntRepository, em);

        System.out.println("✓ Application initialisée avec succès !\n");
    }

    private static void afficherMenuPrincipal() {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║            MENU PRINCIPAL                      ║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.println("1. 📚 Gestion des Livres");
            System.out.println("2. 👥 Gestion des Utilisateurs");
            System.out.println("0. 🚪 Quitter");
            System.out.print("\n➤ Votre choix : ");

            String choix = scanner.nextLine().trim();

            switch (choix) {
                case "1":
                    afficherMenuLivres();
                    break;
                case "2":
                    afficherMenuUtilisateurs();
                    break;
                case "0":
                    System.out.println("\n✓ Fermeture de l'application...");
                    continuer = false;
                    break;
                default:
                    System.out.println("\n✗ Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private static void afficherMenuLivres() {
        BookDisplay bookDisplay = new BookDisplay(bookManager, empruntManager, userManager);
        bookDisplay.afficherMenu();
    }

    private static void afficherMenuUtilisateurs() {
        UserDisplay userDisplay = new UserDisplay(userManager);
        userDisplay.afficherMenu();
    }
}
