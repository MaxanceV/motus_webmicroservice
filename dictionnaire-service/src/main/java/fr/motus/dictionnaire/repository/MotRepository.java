package fr.motus.dictionnaire.repository;

import fr.motus.dictionnaire.model.Mot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotRepository extends JpaRepository<Mot, Long> {

    // Spring Data génère automatiquement la requête SQL à partir du nom de la méthode :
    // "findBy" + "Valeur" + "IgnoreCase" → SELECT * FROM mots WHERE LOWER(valeur) = LOWER(?)
    Optional<Mot> findByValeurIgnoreCase(String valeur);
    boolean existsByValeurIgnoreCase(String valeur);
    List<Mot> findByLongueur(int longueur);

    // Ici Spring Data ne peut pas deviner la requête, donc on l'écrit nous-mêmes.
    // nativeQuery = true : c'est du SQL PostgreSQL direct (pas du JPQL/HQL).
    // ORDER BY RANDOM() : PostgreSQL tire une ligne au hasard parmi celles qui matchent.
    // LIMIT 1 : on ne veut qu'un seul mot — sans ça on récupèrerait toute la table triée aléatoirement.
    @Query(value = "SELECT * FROM mots WHERE longueur = :longueur ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Mot> findRandomByLongueur(int longueur);
}
