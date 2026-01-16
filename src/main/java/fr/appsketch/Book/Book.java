package fr.appsketch.Book;

import fr.appsketch.Emprunt.Emprunt;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String auteur;

    @Column(name = "date_publication")
    private LocalDate datePublication;

    @Column(unique = true)
    private String isbn;

    @Column
    private String categorie;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Emprunt> emprunts = new ArrayList<>();

    // Constructeurs
    public Book() {
    }

    public Book(String titre, String auteur, LocalDate datePublication, String isbn, String categorie) {
        this.titre = titre;
        this.auteur = auteur;
        this.datePublication = datePublication;
        this.isbn = isbn;
        this.categorie = categorie;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDate datePublication) {
        this.datePublication = datePublication;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public List<Emprunt> getEmprunts() {
        return emprunts;
    }

    public void setEmprunts(List<Emprunt> emprunts) {
        this.emprunts = emprunts;
    }

    // Méthode utilitaire pour vérifier si le livre est actuellement emprunté
    public boolean isEmprunte() {
        if (emprunts == null || emprunts.isEmpty()) {
            return false;
        }
        return emprunts.stream()
                .anyMatch(emprunt -> emprunt.getEtat() != null &&
                        emprunt.getEtat().toString().equals("EN_COURS"));
    }

    // Méthode utilitaire pour ajouter un emprunt
    public void addEmprunt(Emprunt emprunt) {
        emprunts.add(emprunt);
        emprunt.setBook(this);
    }

    // Méthode utilitaire pour retirer un emprunt
    public void removeEmprunt(Emprunt emprunt) {
        emprunts.remove(emprunt);
        emprunt.setBook(null);
    }

    @Override
    public String toString() {
        return "Livre{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", auteur='" + auteur + '\'' +
                ", datePublication=" + datePublication +
                ", isbn='" + isbn + '\'' +
                ", categorie='" + categorie + '\'' +
                ", emprunts=" + emprunts.size() +
                '}';
    }
}
