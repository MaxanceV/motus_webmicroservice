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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partie_id")
    @JsonBackReference
    private Partie partie;

    @OneToMany(mappedBy = "tentative", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ResultatLettre> resultats = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.dateHeure = LocalDateTime.now();
    }
}
