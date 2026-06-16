package fr.motus.partie.dto;

import fr.motus.partie.model.Partie;
import fr.motus.partie.model.StatutPartie;
import fr.motus.partie.model.Tentative;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse pour une Partie.
 * motMystere est masqué tant que la partie est EN_COURS,
 * puis révélé une fois GAGNEE ou PERDUE.
 */
@Getter
public class PartieResponseDTO {

    private final Long id;
    private final Long joueurId;
    private final StatutPartie statut;
    private final int nombreEssaisMax;
    private final LocalDateTime dateDebut;
    private final LocalDateTime dateFin;
    private final List<Tentative> tentatives;

    /**
     * Mot mystère : null si EN_COURS (le joueur ne doit pas le voir),
     * valeur réelle si GAGNEE ou PERDUE.
     */
    private final String motMystere;

    public PartieResponseDTO(Partie partie) {
        this.id            = partie.getId();
        this.joueurId      = partie.getJoueurId();
        this.statut        = partie.getStatut();
        this.nombreEssaisMax = partie.getNombreEssaisMax();
        this.dateDebut     = partie.getDateDebut();
        this.dateFin       = partie.getDateFin();
        this.tentatives    = partie.getTentatives();
        // Révèle le mot uniquement quand la partie est terminée
        this.motMystere    = partie.getStatut() == StatutPartie.EN_COURS
                ? null
                : partie.getMotMystere();
    }
}
