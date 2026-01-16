package fr.appsketch.Book;

import java.time.LocalDate;

/**
 * Data Transfer Object pour l'import/export JSON des livres
 */
public class BookDTO {
    private String titre;
    private String auteur;
    private LocalDate datePublication;
    private String isbn;
    private String categorie;

    // Constructeur par défaut (nécessaire pour Gson)
    public BookDTO() {
    }

    public BookDTO(String titre, String auteur, LocalDate datePublication, String isbn, String categorie) {
        this.titre = titre;
        this.auteur = auteur;
        this.datePublication = datePublication;
        this.isbn = isbn;
        this.categorie = categorie;
    }

    // Conversion depuis Book
    public static BookDTO fromBook(Book book) {
        return new BookDTO(
                book.getTitre(),
                book.getAuteur(),
                book.getDatePublication(),
                book.getIsbn(),
                book.getCategorie()
        );
    }

    // Getters et Setters
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
}
