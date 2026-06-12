package fr.motus.partie.repository;

import fr.motus.partie.model.Tentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TentativeRepository extends JpaRepository<Tentative, Long> {
    List<Tentative> findByPartieIdOrderByNumero(Long partieId);
    int countByPartieId(Long partieId);
}
