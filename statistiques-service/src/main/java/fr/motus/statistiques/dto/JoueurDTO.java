package fr.motus.statistiques.dto;

import lombok.Data;

@Data
public class JoueurDTO {
    private Long id;
    private String pseudonyme;
    private String email;
}
