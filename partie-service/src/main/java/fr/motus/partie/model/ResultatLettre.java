package fr.motus.partie.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resultats_lettres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultatLettre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int position;
    private char lettre;

    @Enumerated(EnumType.STRING)
    private EtatLettre etat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tentative_id")
    @JsonBackReference
    private Tentative tentative;
}
