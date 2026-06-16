package fr.motus.partie.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Partie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING) // stocke "EN_COURS" en base plutôt qu'un nombre (plus lisible)
    private StatutPartie statut = StatutPartie.EN_COURS;

    private String motMystere;

    private int nombreEssaisMax = 6;

    // On ne stocke pas l'objet Joueur ici : chaque service a sa propre base de données,
    // donc pas de clé étrangère cross-service. On garde juste l'ID — c'est la bonne
    // pratique en microservices (couplage faible entre services).
    private Long joueurId;

    // @JsonManagedReference / @JsonBackReference : gèrent la sérialisation JSON
    // des relations bidirectionnelles pour éviter une boucle infinie.
    // Ici Partie -> Tentative -> Partie -> Tentative -> ... sans ça = StackOverflow.
    // La "managed" (Partie) sera incluse dans le JSON ; la "back" (Tentative.partie) sera ignorée.
    @OneToMany(mappedBy = "partie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Tentative> tentatives = new ArrayList<>();

    // @PrePersist : méthode appelée automatiquement par JPA juste AVANT le premier INSERT.
    // Pratique pour initialiser des champs calculés sans les mettre dans le constructeur.
    @PrePersist
    public void prePersist() {
        this.dateDebut = LocalDateTime.now();
        this.statut = StatutPartie.EN_COURS;
    }
}
