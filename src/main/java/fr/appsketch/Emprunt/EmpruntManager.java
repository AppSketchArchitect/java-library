package fr.appsketch.Emprunt;

import fr.appsketch.Book.Book;
import fr.appsketch.User.User;
import java.time.LocalDate;


public class EmpruntManager {

    private final EmpruntRepository empruntRepository;

    public EmpruntManager() {
        this.empruntRepository = new EmpruntRepository();
    }

    public Emprunt startEmprunt(User user, Book book) {
        Emprunt emprunt = new Emprunt(user, book, LocalDate.now());

        return empruntRepository.save(emprunt);
    }

    public void endEmprunt(Emprunt emprunt) {
        emprunt.setEtat(EtatEmprunt.TERMINE);
        empruntRepository.save(emprunt);
    }
}
