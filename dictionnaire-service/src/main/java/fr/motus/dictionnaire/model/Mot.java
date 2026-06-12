package fr.motus.dictionnaire.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String valeur;

    private int longueur;

    @PrePersist
    @PreUpdate
    public void computeLongueur() {
        if (this.valeur != null) {
            this.valeur = this.valeur.toUpperCase();
            this.longueur = this.valeur.length();
        }
    }
}
