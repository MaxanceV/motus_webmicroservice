package fr.motus.joueur.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "joueurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String pseudonyme;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String motDePasse;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateInscription;

    @PrePersist
    public void prePersist() {
        this.dateInscription = LocalDateTime.now();
    }
}
