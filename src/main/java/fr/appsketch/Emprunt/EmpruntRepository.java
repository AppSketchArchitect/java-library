package fr.appsketch.Emprunt;

import fr.appsketch.Book.Book;
import fr.appsketch.Core.HibernateManager;
import fr.appsketch.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public class EmpruntRepository {

    @PersistenceContext
    private EntityManager em ;

    public EmpruntRepository() {
        em = HibernateManager.getSessionFactory().createEntityManager();
    }

    @Transactional
    public Emprunt save(Emprunt emprunt) {
        if (emprunt.getId() == null) {
            em.persist(emprunt);
            return emprunt;
        } else {
            return em.merge(emprunt);
        }
    }

    public Optional<Emprunt> findById(Long id) {
        Emprunt emprunt = em.find(Emprunt.class, id);
        return Optional.ofNullable(emprunt);
    }

    public List<Emprunt> findAll() {
        TypedQuery<Emprunt> query = em.createQuery("SELECT e FROM Emprunt e", Emprunt.class);
        return query.getResultList();
    }

    public List<Emprunt> findByUser(User user) {
        TypedQuery<Emprunt> query = em.createQuery(
            "SELECT e FROM Emprunt e WHERE e.user = :user", Emprunt.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public List<Emprunt> findByBook(Book book) {
        TypedQuery<Emprunt> query = em.createQuery(
            "SELECT e FROM Emprunt e WHERE e.book = :book", Emprunt.class);
        query.setParameter("book", book);
        return query.getResultList();
    }

    public List<Emprunt> findByUserId(Long userId) {
        TypedQuery<Emprunt> query = em.createQuery(
            "SELECT e FROM Emprunt e WHERE e.user.id = :userId", Emprunt.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<Emprunt> findByBookId(Long bookId) {
        TypedQuery<Emprunt> query = em.createQuery(
            "SELECT e FROM Emprunt e WHERE e.book.id = :bookId", Emprunt.class);
        query.setParameter("bookId", bookId);
        return query.getResultList();
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(em::remove);
    }
}
