package fr.appsketch.Book;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Manager pour gérer la logique métier des livres
 * Responsabilités: logique métier, validation, gestion des transactions
 */
public class BookManager {

    private final BookRepository bookRepository;
    private final EntityManager em;

    public BookManager(BookRepository bookRepository, EntityManager em) {
        this.bookRepository = bookRepository;
        this.em = em;
    }

    /**
     * Ajoute un nouveau livre en base de données
     */
    public Book ajouterLivre(String titre, String auteur, LocalDate datePublication, String isbn, String categorie) {
        // Validation
        if (titre == null || titre.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (auteur == null || auteur.trim().isEmpty()) {
            throw new IllegalArgumentException("L'auteur est obligatoire");
        }

        // Vérifier si l'ISBN existe déjà
        if (isbn != null && !isbn.isEmpty() && bookRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("Un livre avec cet ISBN existe déjà");
        }

        Book book = new Book(titre, auteur, datePublication, isbn, categorie);

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Book savedBook = bookRepository.save(book);
            transaction.commit();
            return savedBook;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de l'ajout du livre", e);
        }
    }

    /**
     * Modifie un livre existant
     */
    public Book modifierLivre(Long id, String titre, String auteur, LocalDate datePublication, String isbn, String categorie) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isEmpty()) {
            throw new IllegalArgumentException("Livre non trouvé avec l'ID: " + id);
        }

        Book book = optionalBook.get();

        if (titre != null && !titre.isEmpty()) {
            book.setTitre(titre);
        }
        if (auteur != null && !auteur.isEmpty()) {
            book.setAuteur(auteur);
        }
        if (datePublication != null) {
            book.setDatePublication(datePublication);
        }
        if (isbn != null && !isbn.isEmpty()) {
            book.setIsbn(isbn);
        }
        if (categorie != null && !categorie.isEmpty()) {
            book.setCategorie(categorie);
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Book updatedBook = bookRepository.save(book);
            transaction.commit();
            return updatedBook;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la modification du livre", e);
        }
    }

    /**
     * Supprime un livre par son ID
     */
    public void supprimerLivre(Long id) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isEmpty()) {
            throw new IllegalArgumentException("Livre non trouvé avec l'ID: " + id);
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            bookRepository.deleteById(id);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression du livre", e);
        }
    }

    /**
     * Récupère un livre par son ID
     */
    public Optional<Book> trouverParId(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * Récupère tous les livres
     */
    public List<Book> listerTousLesLivres() {
        return bookRepository.findAll();
    }

    /**
     * Recherche des livres par titre (contient)
     */
    public List<Book> rechercherParTitre(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            return List.of();
        }
        return bookRepository.findAll().stream()
                .filter(book -> book.getTitre().toLowerCase().contains(titre.toLowerCase()))
                .toList();
    }

    /**
     * Recherche des livres par auteur (contient)
     */
    public List<Book> rechercherParAuteur(String auteur) {
        if (auteur == null || auteur.trim().isEmpty()) {
            return List.of();
        }
        return bookRepository.findAll().stream()
                .filter(book -> book.getAuteur().toLowerCase().contains(auteur.toLowerCase()))
                .toList();
    }

    /**
     * Recherche des livres par catégorie (contient)
     */
    public List<Book> rechercherParCategorie(String categorie) {
        if (categorie == null || categorie.trim().isEmpty()) {
            return List.of();
        }
        return bookRepository.findAll().stream()
                .filter(book -> book.getCategorie() != null &&
                        book.getCategorie().toLowerCase().contains(categorie.toLowerCase()))
                .toList();
    }

    /**
     * Vérifie si un ISBN existe déjà
     */
    public boolean isbnExiste(String isbn) {
        return isbn != null && !isbn.isEmpty() && bookRepository.existsByIsbn(isbn);
    }
}
