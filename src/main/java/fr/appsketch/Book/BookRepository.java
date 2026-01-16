package fr.appsketch.Book;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour gérer la persistance des livres
 * Responsabilités: accès aux données, requêtes SQL/JPQL
 */
public class BookRepository {

    private final EntityManager em;

    public BookRepository(EntityManager em) {
        this.em = em;
    }

    public Book save(Book book) {
        if (book.getId() == null) {
            em.persist(book);
            return book;
        } else {
            return em.merge(book);
        }
    }

    public Optional<Book> findById(Long id) {
        Book book = em.find(Book.class, id);
        return Optional.ofNullable(book);
    }

    public List<Book> findAll() {
        TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b", Book.class);
        return query.getResultList();
    }

    public Optional<Book> findByIsbn(String isbn) {
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class);
        query.setParameter("isbn", isbn);
        return query.getResultStream().findFirst();
    }

    public boolean existsByIsbn(String isbn) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn", Long.class);
        query.setParameter("isbn", isbn);
        return query.getSingleResult() > 0;
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(em::remove);
    }
}
