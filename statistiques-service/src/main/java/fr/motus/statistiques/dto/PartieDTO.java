package fr.motus.statistiques.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PartieDTO {
    private Long id;
    private Long joueurId;
    private String statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private int nombreEssaisMax;
}
