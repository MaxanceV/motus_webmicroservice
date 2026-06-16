package fr.motus.partie.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tentatives")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tentative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numero;
    private String motPropose;
    private LocalDateTime dateHeure;

    // @JsonBackReference : côté "enfant" de la relation bidirectionnelle.
    // Ce champ (partie) sera ignoré lors de la sérialisation JSON pour casser
    // la boucle Tentative -> Partie -> Tentative -> ...
    // FetchType.LAZY = la partie n'est chargée en base que si on y accède vraiment
    // (optimisation — évite un SELECT inutile à chaque chargement de tentative).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partie_id")
    @JsonBackReference
    private Partie partie;

    // FetchType.EAGER ici : les résultats lettre par lettre sont toujours chargés
    // avec la tentative — c'est ce qu'on veut puisqu'on les affiche toujours ensemble.
    @OneToMany(mappedBy = "tentative", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ResultatLettre> resultats = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.dateHeure = LocalDateTime.now();
    }
}
