package fr.motus.joueur.repository;

import fr.motus.joueur.model.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JoueurRepository extends JpaRepository<Joueur, Long> {
    Optional<Joueur> findByEmail(String email);
    boolean existsByEmail(String email);
}
