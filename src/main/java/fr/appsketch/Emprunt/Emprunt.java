package fr.appsketch.Emprunt;

import fr.appsketch.Book.Book;
import fr.appsketch.User.User;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Emprunts")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "date_emprunt", nullable = false)
    private LocalDate dateEmprunt;

    public Emprunt() {
    }

    public Emprunt(User user, Book book, LocalDate dateEmprunt) {
        this.user = user;
        this.book = book;
        this.dateEmprunt = dateEmprunt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getDateEmprunt() {
        return dateEmprunt;
    }

    public void setDateEmprunt(LocalDate dateEmprunt) {
        this.dateEmprunt = dateEmprunt;
    }
}
