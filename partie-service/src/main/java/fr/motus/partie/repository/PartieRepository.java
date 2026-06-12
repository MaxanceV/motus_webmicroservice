package fr.motus.partie.repository;

import fr.motus.partie.model.Partie;
import fr.motus.partie.model.StatutPartie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartieRepository extends JpaRepository<Partie, Long> {
    List<Partie> findByJoueurId(Long joueurId);
    List<Partie> findByStatut(StatutPartie statut);
    List<Partie> findByJoueurIdAndStatut(Long joueurId, StatutPartie statut);
}
