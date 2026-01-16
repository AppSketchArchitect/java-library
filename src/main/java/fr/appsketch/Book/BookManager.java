package fr.appsketch.Book;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class BookManager {

    private final BookRepository bookRepository;
    private final EntityManager em;

    public BookManager(BookRepository bookRepository, EntityManager em) {
        this.bookRepository = bookRepository;
        this.em = em;
    }

    public Book creerLivre() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Création d'un nouveau livre");

        System.out.print("Titre : ");
        String titre = scanner.nextLine().trim();

        System.out.print("Auteur : ");
        String auteur = scanner.nextLine().trim();

        LocalDate datePublication = null;
        while (datePublication == null) {
            try {
                System.out.print("Date de publication (YYYY-MM-DD) : ");
                String dateStr = scanner.nextLine().trim();
                datePublication = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                System.out.println("Format de date invalide. Réessayez.");
            }
        }

        System.out.print("ISBN : ");
        String isbn = scanner.nextLine().trim();

        if (bookRepository.existsByIsbn(isbn)) {
            System.out.println("Erreur : ce livre existe déjà (ISBN unique) !");
            return null;
        }

        System.out.print("Catégorie : ");
        String categorie = scanner.nextLine().trim();

        Book book = new Book(titre, auteur, datePublication, isbn, categorie);

        // Transaction pour enregistrer le livre
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            bookRepository.save(book);
            transaction.commit();
            System.out.println("Livre créé et enregistré : " + book);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la création du livre : " + e.getMessage());
            return null;
        }

        return book;
    }

    public void afficherLivres() {
        List<Book> livres = bookRepository.findAll();

        System.out.println("\n--- Livres en base ---");
        for (Book b : livres) {
            System.out.println(b);
        }
        System.out.println("----------------------\n");
    }

    public static void main(String[] args) {
        BookRepository repo = new BookRepository();
        EntityManager em = repo.getEm();

        BookManager manager = new BookManager(repo, em);

        try {
            manager.creerLivre();
            manager.afficherLivres();
        } finally {
            em.close();
        }
    }
}

