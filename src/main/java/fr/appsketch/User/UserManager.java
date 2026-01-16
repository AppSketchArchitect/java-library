package fr.appsketch.User;

import fr.appsketch.Core.HibernateManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Scanner;
import java.util.List;

public class UserManager {

    private UserRepository userRepository;
    private EntityManager em;

    public UserManager(UserRepository userRepository, EntityManager em) {
        this.userRepository = userRepository;
        this.em = em;
    }

    public User creerUtilisateur() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Création d'un nouvel utilisateur");

        System.out.print("Nom : ");
        String nom = scanner.nextLine().trim();

        System.out.print("Prénom : ");
        String prenom = scanner.nextLine().trim();

        System.out.print("Email : ");
        String email = scanner.nextLine().trim();

        if (userRepository.existsByEmail(email)) {
            System.out.println("Erreur : cet email existe déjà !");
            return null;
        }

        System.out.print("Mot de passe : ");
        String motDePasse = scanner.nextLine().trim();

        User user = new User(nom, prenom, email, motDePasse);

        // Sauvegarder en base via le repository avec transaction
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            userRepository.save(user);
            transaction.commit();
            System.out.println("Utilisateur créé et enregistré : " + user);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la création de l'utilisateur : " + e.getMessage());
            return null;
        }

        return user;
    }

    public void afficherUtilisateurs() {
        List<User> utilisateurs = userRepository.findAll();

        System.out.println("\n--- Utilisateurs en base ---");
        for (User u : utilisateurs) {
            System.out.println(u);
        }
        System.out.println("----------------------------\n");
    }

    // Exemple de test
    public static void main(String[] args) {
        EntityManager em = HibernateManager.getSessionFactory().createEntityManager();
        UserRepository repo = new UserRepository(em);
        UserManager manager = new UserManager(repo, em);

        try {
            manager.creerUtilisateur();
            manager.afficherUtilisateurs();
        } finally {
            em.close();
            HibernateManager.shutdown();
        }
    }
}
