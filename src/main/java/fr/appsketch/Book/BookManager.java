package fr.appsketch.Book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
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
            em.flush(); // Force la synchronisation avec la base
            transaction.commit();
            em.clear(); // Vide le cache de premier niveau
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
            em.flush(); // Force la synchronisation avec la base
            transaction.commit();
            em.clear(); // Vide le cache de premier niveau
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
            em.flush(); // Force la synchronisation avec la base AVANT le commit
            transaction.commit();
            em.clear(); // Vide le cache de premier niveau APRÈS le commit
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
     * Récupère les livres disponibles (non empruntés)
     */
    public List<Book> listerLivresDisponibles() {
        return bookRepository.findAll().stream()
                .filter(book -> !book.isEmprunte())
                .toList();
    }

    /**
     * Récupère les livres empruntés
     */
    public List<Book> listerLivresEmpruntes() {
        return bookRepository.findAll().stream()
                .filter(Book::isEmprunte)
                .toList();
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

    /**
     * Exporte la liste des livres vers un fichier JSON
     */
    public void exporterVersJson(String cheminFichier) throws IOException {
        List<Book> livres = bookRepository.findAll();
        List<BookDTO> livresDTO = new ArrayList<>();

        for (Book book : livres) {
            livresDTO.add(BookDTO.fromBook(book));
        }

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        try (FileWriter writer = new FileWriter(cheminFichier)) {
            gson.toJson(livresDTO, writer);
        }
    }

    /**
     * Importe des livres depuis un fichier JSON
     * @return Le nombre de livres importés avec succès
     */
    public int importerDepuisJson(String cheminFichier) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        Type listType = new TypeToken<List<BookDTO>>(){}.getType();
        List<BookDTO> livresDTO;

        try (FileReader reader = new FileReader(cheminFichier)) {
            livresDTO = gson.fromJson(reader, listType);
        } catch (IOException e) {
            // Si le fichier n'existe pas ou n'est pas accessible, retourner 0
            System.out.println("⚠ Impossible de lire le fichier: " + e.getMessage());
            return 0;
        }

        if (livresDTO == null || livresDTO.isEmpty()) {
            return 0;
        }

        int compteur = 0;
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            for (BookDTO dto : livresDTO) {
                // Vérifier si un livre avec le même ISBN existe déjà
                if (dto.getIsbn() != null && !dto.getIsbn().isEmpty()
                    && bookRepository.existsByIsbn(dto.getIsbn())) {
                    System.out.println("⚠ Livre ignoré (ISBN existe déjà): " + dto.getTitre());
                    continue;
                }

                Book book = new Book(
                    dto.getTitre(),
                    dto.getAuteur(),
                    dto.getDatePublication(),
                    dto.getIsbn(),
                    dto.getCategorie()
                );

                bookRepository.save(book);
                compteur++;
            }

            em.flush();
            transaction.commit();
            em.clear();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de l'import des livres", e);
        }

        return compteur;
    }
}
