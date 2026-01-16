package fr.appsketch.Emprunt;

import fr.appsketch.Book.Book;
import fr.appsketch.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Manager pour gérer la logique métier des emprunts
 * Responsabilités: logique métier, validation, gestion des transactions
 */
public class EmpruntManager {

    private final EmpruntRepository empruntRepository;
    private final EntityManager em;

    public EmpruntManager(EmpruntRepository empruntRepository, EntityManager em) {
        this.empruntRepository = empruntRepository;
        this.em = em;
    }

    /**
     * Emprunte un livre pour un utilisateur
     */
    public Emprunt emprunterLivre(User user, Book book) {
        // Validation
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur est obligatoire");
        }
        if (book == null) {
            throw new IllegalArgumentException("Le livre est obligatoire");
        }

        // Vérifier si le livre est déjà emprunté via le repository
        List<Emprunt> empruntsEnCours = empruntRepository.findEmpruntsEnCoursByBook(book);
        if (!empruntsEnCours.isEmpty()) {
            throw new IllegalArgumentException("Ce livre est déjà emprunté");
        }

        Emprunt emprunt = new Emprunt(user, book, LocalDate.now());

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Emprunt savedEmprunt = empruntRepository.save(emprunt);
            transaction.commit();
            return savedEmprunt;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de l'emprunt du livre", e);
        }
    }

    /**
     * Retourne un livre (termine l'emprunt en cours)
     */
    public void rendreLivre(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Le livre est obligatoire");
        }

        // Trouver l'emprunt en cours pour ce livre
        List<Emprunt> empruntsEnCours = empruntRepository.findEmpruntsEnCoursByBook(book);

        if (empruntsEnCours.isEmpty()) {
            throw new IllegalArgumentException("Ce livre n'est pas actuellement emprunté");
        }

        // Terminer le premier emprunt en cours trouvé
        Emprunt emprunt = empruntsEnCours.get(0);
        emprunt.setEtat(EtatEmprunt.TERMINE);

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            empruntRepository.save(emprunt);
            transaction.commit();
            em.flush(); // Force la synchronisation avec la base
            em.clear(); // Vide le cache de premier niveau
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors du retour du livre", e);
        }
    }

    /**
     * Récupère tous les emprunts
     */
    public List<Emprunt> listerTousLesEmprunts() {
        return empruntRepository.findAll();
    }

    /**
     * Récupère les emprunts d'un utilisateur
     */
    public List<Emprunt> listerEmpruntsParUtilisateur(Long userId) {
        return empruntRepository.findByUserId(userId);
    }

    /**
     * Récupère les emprunts d'un livre
     */
    public List<Emprunt> listerEmpruntsParLivre(Long bookId) {
        return empruntRepository.findByBookId(bookId);
    }

    /**
     * Récupère l'emprunt en cours d'un livre
     */
    public Optional<Emprunt> getEmpruntEnCours(Book book) {
        List<Emprunt> empruntsEnCours = empruntRepository.findEmpruntsEnCoursByBook(book);
        return empruntsEnCours.isEmpty() ? Optional.empty() : Optional.of(empruntsEnCours.get(0));
    }
}
