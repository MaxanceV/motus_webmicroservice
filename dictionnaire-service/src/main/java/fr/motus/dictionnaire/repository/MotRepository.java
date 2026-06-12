package fr.motus.dictionnaire.repository;

import fr.motus.dictionnaire.model.Mot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotRepository extends JpaRepository<Mot, Long> {
    Optional<Mot> findByValeurIgnoreCase(String valeur);
    boolean existsByValeurIgnoreCase(String valeur);
    List<Mot> findByLongueur(int longueur);

    @Query(value = "SELECT * FROM mots WHERE longueur = :longueur ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Mot> findRandomByLongueur(int longueur);
}
