package fr.appsketch;

import fr.appsketch.Book.Book;
import fr.appsketch.Book.BookManager;
import fr.appsketch.Book.BookRepository;
import fr.appsketch.Core.HibernateManager;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Classe d'affichage pour la gestion des livres
 * Responsabilités: interface utilisateur, saisie, affichage
 */
public class BookDisplay {

    private final BookManager bookManager;
    private final Scanner scanner;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public BookDisplay(BookManager bookManager) {
        this.bookManager = bookManager;
        this.scanner = new Scanner(System.in);
    }

    public void afficherMenu() {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     GESTION DES LIVRES - MENU          ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Ajouter un livre");
            System.out.println("2. Modifier un livre");
            System.out.println("3. Supprimer un livre");
            System.out.println("4. Lister tous les livres");
            System.out.println("5. Rechercher par titre");
            System.out.println("6. Rechercher par auteur");
            System.out.println("7. Rechercher par catégorie");
            System.out.println("0. Quitter");
            System.out.print("\nVotre choix : ");

            String choix = scanner.nextLine().trim();

            switch (choix) {
                case "1":
                    ajouterLivre();
                    break;
                case "2":
                    modifierLivre();
                    break;
                case "3":
                    supprimerLivre();
                    break;
                case "4":
                    listerTousLesLivres();
                    break;
                case "5":
                    rechercherParTitre();
                    break;
                case "6":
                    rechercherParAuteur();
                    break;
                case "7":
                    rechercherParCategorie();
                    break;
                case "0":
                    System.out.println("\n✓ Au revoir !");
                    continuer = false;
                    break;
                default:
                    System.out.println("\n✗ Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private void ajouterLivre() {
        System.out.println("\n--- AJOUTER UN LIVRE ---");

        System.out.print("Titre : ");
        String titre = scanner.nextLine().trim();

        System.out.print("Auteur : ");
        String auteur = scanner.nextLine().trim();

        System.out.print("Date de publication (jj/mm/aaaa) : ");
        LocalDate datePublication = null;
        try {
            String dateStr = scanner.nextLine().trim();
            if (!dateStr.isEmpty()) {
                datePublication = LocalDate.parse(dateStr, dateFormatter);
            }
        } catch (DateTimeParseException e) {
            System.out.println("⚠ Format de date invalide, la date ne sera pas enregistrée.");
        }

        System.out.print("ISBN : ");
        String isbn = scanner.nextLine().trim();

        if (!isbn.isEmpty() && bookManager.isbnExiste(isbn)) {
            System.out.println("\n✗ Erreur : Un livre avec cet ISBN existe déjà !");
            return;
        }

        System.out.print("Catégorie : ");
        String categorie = scanner.nextLine().trim();

        try {
            Book book = bookManager.ajouterLivre(titre, auteur, datePublication, isbn, categorie);
            System.out.println("\n✓ Livre ajouté avec succès !");
            afficherLivre(book);
        } catch (Exception e) {
            System.err.println("\n✗ Erreur : " + e.getMessage());
        }
    }

    private void modifierLivre() {
        System.out.println("\n--- MODIFIER UN LIVRE ---");

        listerTousLesLivres();

        System.out.print("\nID du livre à modifier : ");
        try {
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<Book> optionalBook = bookManager.trouverParId(id);
            if (optionalBook.isEmpty()) {
                System.out.println("\n✗ Livre non trouvé !");
                return;
            }

            Book book = optionalBook.get();
            System.out.println("\nLivre actuel :");
            afficherLivre(book);

            System.out.println("\n(Appuyez sur Entrée pour garder la valeur actuelle)");

            System.out.print("Nouveau titre [" + book.getTitre() + "] : ");
            String titre = scanner.nextLine().trim();

            System.out.print("Nouvel auteur [" + book.getAuteur() + "] : ");
            String auteur = scanner.nextLine().trim();

            System.out.print("Nouvelle date (jj/mm/aaaa) [" +
                    (book.getDatePublication() != null ? book.getDatePublication().format(dateFormatter) : "non définie") + "] : ");
            String dateStr = scanner.nextLine().trim();
            LocalDate datePublication = null;
            if (!dateStr.isEmpty()) {
                try {
                    datePublication = LocalDate.parse(dateStr, dateFormatter);
                } catch (DateTimeParseException e) {
                    System.out.println("⚠ Format de date invalide, ancienne valeur conservée.");
                }
            }

            System.out.print("Nouvel ISBN [" + book.getIsbn() + "] : ");
            String isbn = scanner.nextLine().trim();

            System.out.print("Nouvelle catégorie [" + book.getCategorie() + "] : ");
            String categorie = scanner.nextLine().trim();

            try {
                Book updatedBook = bookManager.modifierLivre(id, titre, auteur, datePublication, isbn, categorie);
                System.out.println("\n✓ Livre modifié avec succès !");
                afficherLivre(updatedBook);
            } catch (Exception e) {
                System.err.println("\n✗ Erreur : " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("\n✗ ID invalide !");
        }
    }

    private void supprimerLivre() {
        System.out.println("\n--- SUPPRIMER UN LIVRE ---");

        listerTousLesLivres();

        System.out.print("\nID du livre à supprimer : ");
        try {
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<Book> optionalBook = bookManager.trouverParId(id);
            if (optionalBook.isEmpty()) {
                System.out.println("\n✗ Livre non trouvé !");
                return;
            }

            Book book = optionalBook.get();
            System.out.println("\nLivre à supprimer :");
            afficherLivre(book);

            System.out.print("\nConfirmez la suppression (oui/non) : ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (confirmation.equals("oui") || confirmation.equals("o")) {
                try {
                    bookManager.supprimerLivre(id);
                    System.out.println("\n✓ Livre supprimé avec succès !");
                } catch (Exception e) {
                    System.err.println("\n✗ Erreur : " + e.getMessage());
                }
            } else {
                System.out.println("\n⚠ Suppression annulée.");
            }

        } catch (NumberFormatException e) {
            System.out.println("\n✗ ID invalide !");
        }
    }

    private void listerTousLesLivres() {
        System.out.println("\n--- TOUS LES LIVRES ---");
        List<Book> livres = bookManager.listerTousLesLivres();

        if (livres.isEmpty()) {
            System.out.println("Aucun livre enregistré.");
        } else {
            afficherListeLivres(livres);
        }
    }

    private void rechercherParTitre() {
        System.out.println("\n--- RECHERCHE PAR TITRE ---");
        System.out.print("Titre recherché : ");
        String titre = scanner.nextLine().trim();

        List<Book> resultats = bookManager.rechercherParTitre(titre);

        if (resultats.isEmpty()) {
            System.out.println("\n✗ Aucun livre trouvé avec ce titre.");
        } else {
            System.out.println("\n" + resultats.size() + " résultat(s) trouvé(s) :");
            afficherListeLivres(resultats);
        }
    }

    private void rechercherParAuteur() {
        System.out.println("\n--- RECHERCHE PAR AUTEUR ---");
        System.out.print("Auteur recherché : ");
        String auteur = scanner.nextLine().trim();

        List<Book> resultats = bookManager.rechercherParAuteur(auteur);

        if (resultats.isEmpty()) {
            System.out.println("\n✗ Aucun livre trouvé pour cet auteur.");
        } else {
            System.out.println("\n" + resultats.size() + " résultat(s) trouvé(s) :");
            afficherListeLivres(resultats);
        }
    }

    private void rechercherParCategorie() {
        System.out.println("\n--- RECHERCHE PAR CATÉGORIE ---");
        System.out.print("Catégorie recherchée : ");
        String categorie = scanner.nextLine().trim();

        List<Book> resultats = bookManager.rechercherParCategorie(categorie);

        if (resultats.isEmpty()) {
            System.out.println("\n✗ Aucun livre trouvé dans cette catégorie.");
        } else {
            System.out.println("\n" + resultats.size() + " résultat(s) trouvé(s) :");
            afficherListeLivres(resultats);
        }
    }

    private void afficherListeLivres(List<Book> livres) {
        System.out.println("\n" + "=".repeat(90));
        System.out.printf("%-5s %-35s %-25s %-15s %-15s%n",
                "ID", "Titre", "Auteur", "ISBN", "Catégorie");
        System.out.println("=".repeat(90));

        for (Book book : livres) {
            System.out.printf("%-5d %-35s %-25s %-15s %-15s%n",
                    book.getId(),
                    truncate(book.getTitre(), 35),
                    truncate(book.getAuteur(), 25),
                    truncate(book.getIsbn() != null ? book.getIsbn() : "N/A", 15),
                    truncate(book.getCategorie() != null ? book.getCategorie() : "N/A", 15));
        }
        System.out.println("=".repeat(90));
        System.out.println("Total : " + livres.size() + " livre(s)");
    }

    private void afficherLivre(Book book) {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│ ID          : " + book.getId());
        System.out.println("│ Titre       : " + book.getTitre());
        System.out.println("│ Auteur      : " + book.getAuteur());
        System.out.println("│ Date pub.   : " +
                (book.getDatePublication() != null ? book.getDatePublication().format(dateFormatter) : "Non définie"));
        System.out.println("│ ISBN        : " + (book.getIsbn() != null ? book.getIsbn() : "N/A"));
        System.out.println("│ Catégorie   : " + (book.getCategorie() != null ? book.getCategorie() : "N/A"));
        System.out.println("└─────────────────────────────────────────┘");
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "N/A";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }

    public static void main(String[] args) {
        EntityManager em = HibernateManager.getSessionFactory().createEntityManager();
        BookRepository bookRepository = new BookRepository(em);
        BookManager bookManager = new BookManager(bookRepository, em);
        BookDisplay bookDisplay = new BookDisplay(bookManager);

        try {
            bookDisplay.afficherMenu();
        } finally {
            em.close();
            HibernateManager.shutdown();
        }
    }
}
