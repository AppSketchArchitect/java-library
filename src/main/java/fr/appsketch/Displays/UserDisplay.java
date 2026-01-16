package fr.appsketch.Displays;

import fr.appsketch.User.User;
import fr.appsketch.User.UserManager;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Classe d'affichage pour la gestion des utilisateurs
 * Responsabilités: interface utilisateur, saisie, affichage
 */
public class UserDisplay {

    private final UserManager userManager;
    private final Scanner scanner;

    public UserDisplay(UserManager userManager) {
        this.userManager = userManager;
        this.scanner = new Scanner(System.in);
    }

    public void afficherMenu() {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   GESTION DES UTILISATEURS - MENU      ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Ajouter un utilisateur");
            System.out.println("2. Modifier un utilisateur");
            System.out.println("3. Supprimer un utilisateur");
            System.out.println("4. Lister tous les utilisateurs");
            System.out.println("5. Rechercher par email");
            System.out.println("6. Rechercher par nom");
            System.out.println("7. Rechercher par prénom");
            System.out.println("0. Quitter");
            System.out.print("\nVotre choix : ");

            String choix = scanner.nextLine().trim();

            switch (choix) {
                case "1":
                    ajouterUtilisateur();
                    break;
                case "2":
                    modifierUtilisateur();
                    break;
                case "3":
                    supprimerUtilisateur();
                    break;
                case "4":
                    listerTousLesUtilisateurs();
                    break;
                case "5":
                    rechercherParEmail();
                    break;
                case "6":
                    rechercherParNom();
                    break;
                case "7":
                    rechercherParPrenom();
                    break;
                case "0":
                    System.out.println("\n✓ Retour au menu principal...");
                    continuer = false;
                    break;
                default:
                    System.out.println("\n✗ Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private void ajouterUtilisateur() {
        System.out.println("\n--- AJOUTER UN UTILISATEUR ---");

        System.out.print("Nom : ");
        String nom = scanner.nextLine().trim();

        System.out.print("Prénom : ");
        String prenom = scanner.nextLine().trim();

        System.out.print("Email : ");
        String email = scanner.nextLine().trim();

        if (!email.isEmpty() && userManager.emailExiste(email)) {
            System.out.println("\n✗ Erreur : Un utilisateur avec cet email existe déjà !");
            return;
        }

        System.out.print("Mot de passe : ");
        String motDePasse = scanner.nextLine().trim();

        try {
            User user = userManager.ajouterUtilisateur(nom, prenom, email, motDePasse);
            System.out.println("\n✓ Utilisateur ajouté avec succès !");
            afficherUtilisateur(user);
        } catch (Exception e) {
            System.err.println("\n✗ Erreur : " + e.getMessage());
        }
    }

    private void modifierUtilisateur() {
        System.out.println("\n--- MODIFIER UN UTILISATEUR ---");

        listerTousLesUtilisateurs();

        System.out.print("\nID de l'utilisateur à modifier : ");
        try {
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<User> optionalUser = userManager.trouverParId(id);
            if (optionalUser.isEmpty()) {
                System.out.println("\n✗ Utilisateur non trouvé !");
                return;
            }

            User user = optionalUser.get();
            System.out.println("\nUtilisateur actuel :");
            afficherUtilisateur(user);

            System.out.println("\n(Appuyez sur Entrée pour garder la valeur actuelle)");

            System.out.print("Nouveau nom [" + user.getNom() + "] : ");
            String nom = scanner.nextLine().trim();

            System.out.print("Nouveau prénom [" + user.getPrenom() + "] : ");
            String prenom = scanner.nextLine().trim();

            System.out.print("Nouvel email [" + user.getEmail() + "] : ");
            String email = scanner.nextLine().trim();

            System.out.print("Nouveau mot de passe [****] : ");
            String motDePasse = scanner.nextLine().trim();

            try {
                User updatedUser = userManager.modifierUtilisateur(id, nom, prenom, email, motDePasse);
                System.out.println("\n✓ Utilisateur modifié avec succès !");
                afficherUtilisateur(updatedUser);
            } catch (Exception e) {
                System.err.println("\n✗ Erreur : " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("\n✗ ID invalide !");
        }
    }

    private void supprimerUtilisateur() {
        System.out.println("\n--- SUPPRIMER UN UTILISATEUR ---");

        listerTousLesUtilisateurs();

        System.out.print("\nID de l'utilisateur à supprimer : ");
        try {
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<User> optionalUser = userManager.trouverParId(id);
            if (optionalUser.isEmpty()) {
                System.out.println("\n✗ Utilisateur non trouvé !");
                return;
            }

            User user = optionalUser.get();
            System.out.println("\nUtilisateur à supprimer :");
            afficherUtilisateur(user);

            System.out.print("\nConfirmez la suppression (oui/non) : ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (confirmation.equals("oui") || confirmation.equals("o")) {
                try {
                    userManager.supprimerUtilisateur(id);
                    System.out.println("\n✓ Utilisateur supprimé avec succès !");
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

    private void listerTousLesUtilisateurs() {
        System.out.println("\n--- TOUS LES UTILISATEURS ---");
        List<User> utilisateurs = userManager.listerTousLesUtilisateurs();

        if (utilisateurs.isEmpty()) {
            System.out.println("Aucun utilisateur enregistré.");
        } else {
            afficherListeUtilisateurs(utilisateurs);
        }
    }

    private void rechercherParEmail() {
        System.out.println("\n--- RECHERCHE PAR EMAIL ---");
        System.out.print("Email recherché : ");
        String email = scanner.nextLine().trim();

        Optional<User> optionalUser = userManager.rechercherParEmail(email);

        if (optionalUser.isEmpty()) {
            System.out.println("\n✗ Aucun utilisateur trouvé avec cet email.");
        } else {
            System.out.println("\nUtilisateur trouvé :");
            afficherUtilisateur(optionalUser.get());
        }
    }

    private void rechercherParNom() {
        System.out.println("\n--- RECHERCHE PAR NOM ---");
        System.out.print("Nom recherché : ");
        String nom = scanner.nextLine().trim();

        List<User> resultats = userManager.rechercherParNom(nom);

        if (resultats.isEmpty()) {
            System.out.println("\n✗ Aucun utilisateur trouvé avec ce nom.");
        } else {
            System.out.println("\n" + resultats.size() + " résultat(s) trouvé(s) :");
            afficherListeUtilisateurs(resultats);
        }
    }

    private void rechercherParPrenom() {
        System.out.println("\n--- RECHERCHE PAR PRÉNOM ---");
        System.out.print("Prénom recherché : ");
        String prenom = scanner.nextLine().trim();

        List<User> resultats = userManager.rechercherParPrenom(prenom);

        if (resultats.isEmpty()) {
            System.out.println("\n✗ Aucun utilisateur trouvé avec ce prénom.");
        } else {
            System.out.println("\n" + resultats.size() + " résultat(s) trouvé(s) :");
            afficherListeUtilisateurs(resultats);
        }
    }

    private void afficherListeUtilisateurs(List<User> utilisateurs) {
        System.out.println("\n" + "=".repeat(80));
        System.out.printf("%-5s %-20s %-20s %-35s%n",
                "ID", "Nom", "Prénom", "Email");
        System.out.println("=".repeat(80));

        for (User user : utilisateurs) {
            System.out.printf("%-5d %-20s %-20s %-35s%n",
                    user.getId(),
                    truncate(user.getNom(), 20),
                    truncate(user.getPrenom(), 20),
                    truncate(user.getEmail(), 35));
        }
        System.out.println("=".repeat(80));
        System.out.println("Total : " + utilisateurs.size() + " utilisateur(s)");
    }

    private void afficherUtilisateur(User user) {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│ ID          : " + user.getId());
        System.out.println("│ Nom         : " + user.getNom());
        System.out.println("│ Prénom      : " + user.getPrenom());
        System.out.println("│ Email       : " + user.getEmail());
        System.out.println("│ Mot de passe: ****");
        System.out.println("└─────────────────────────────────────────┘");
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "N/A";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}
