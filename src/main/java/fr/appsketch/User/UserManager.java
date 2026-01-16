package fr.appsketch.User;

import java.util.Scanner;
import java.util.List;

public class UserManager {

    private UserRepository userRepository;

    public UserManager(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        // Sauvegarder en base via le repository
        userRepository.save(user);

        System.out.println("Utilisateur créé et enregistré : " + user);
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
        UserRepository repo = new UserRepository();
        UserManager manager = new UserManager(repo);

        manager.creerUtilisateur();
        manager.afficherUtilisateurs();
    }
}
