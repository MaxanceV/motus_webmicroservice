package fr.motus.statistiques.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatJoueurDTO {
    private Long joueurId;
    private String pseudonyme;
    private long totalParties;
    private long partiesGagnees;
    private long partiesPerdues;
    private double tauxVictoire;
}
