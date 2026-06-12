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

    @Enumerated(EnumType.STRING)
    private StatutPartie statut = StatutPartie.EN_COURS;

    private String motMystere;

    private int nombreEssaisMax = 6;

    // Référence au joueur dans joueur-service (pas de @ManyToOne cross-service)
    private Long joueurId;

    @OneToMany(mappedBy = "partie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Tentative> tentatives = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.dateDebut = LocalDateTime.now();
        this.statut = StatutPartie.EN_COURS;
    }
}
