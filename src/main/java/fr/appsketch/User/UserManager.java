package fr.appsketch.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Manager pour gérer la logique métier des utilisateurs
 * Responsabilités: logique métier, validation, gestion des transactions
 */
public class UserManager {

    private final UserRepository userRepository;
    private final EntityManager em;

    public UserManager(UserRepository userRepository, EntityManager em) {
        this.userRepository = userRepository;
        this.em = em;
    }

    /**
     * Ajoute un nouvel utilisateur en base de données
     */
    public User ajouterUtilisateur(String nom, String prenom, String email, String motDePasse) {
        // Validation
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        if (motDePasse == null || motDePasse.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        User user = new User(nom, prenom, email, motDePasse);

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            User savedUser = userRepository.save(user);
            transaction.commit();
            return savedUser;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de l'ajout de l'utilisateur", e);
        }
    }

    /**
     * Modifie un utilisateur existant
     */
    public User modifierUtilisateur(Long id, String nom, String prenom, String email, String motDePasse) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + id);
        }

        User user = optionalUser.get();

        if (nom != null && !nom.isEmpty()) {
            user.setNom(nom);
        }
        if (prenom != null && !prenom.isEmpty()) {
            user.setPrenom(prenom);
        }
        if (email != null && !email.isEmpty()) {
            // Vérifier que le nouvel email n'existe pas déjà (sauf si c'est le même)
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre utilisateur");
            }
            user.setEmail(email);
        }
        if (motDePasse != null && !motDePasse.isEmpty()) {
            user.setMotDePasse(motDePasse);
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            User updatedUser = userRepository.save(user);
            transaction.commit();
            return updatedUser;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la modification de l'utilisateur", e);
        }
    }

    /**
     * Supprime un utilisateur par son ID
     */
    public void supprimerUtilisateur(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + id);
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            userRepository.deleteById(id);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }

    /**
     * Récupère un utilisateur par son ID
     */
    public Optional<User> trouverParId(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Récupère tous les utilisateurs
     */
    public List<User> listerTousLesUtilisateurs() {
        return userRepository.findAll();
    }

    /**
     * Recherche un utilisateur par email
     */
    public Optional<User> rechercherParEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email);
    }

    /**
     * Recherche des utilisateurs par nom (contient)
     */
    public List<User> rechercherParNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.findAll().stream()
                .filter(user -> user.getNom().toLowerCase().contains(nom.toLowerCase()))
                .toList();
    }

    /**
     * Recherche des utilisateurs par prénom (contient)
     */
    public List<User> rechercherParPrenom(String prenom) {
        if (prenom == null || prenom.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.findAll().stream()
                .filter(user -> user.getPrenom().toLowerCase().contains(prenom.toLowerCase()))
                .toList();
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExiste(String email) {
        return email != null && !email.isEmpty() && userRepository.existsByEmail(email);
    }
}
