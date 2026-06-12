package fr.motus.statistiques.service;

import fr.motus.statistiques.dto.JoueurDTO;
import fr.motus.statistiques.dto.PartieDTO;
import fr.motus.statistiques.dto.StatJoueurDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatistiquesService {

    private final RestClient partieClient;
    private final RestClient joueurClient;

    public StatistiquesService(
            @Value("${partie.service.url}") String partieUrl,
            @Value("${joueur.service.url}") String joueurUrl) {
        this.partieClient = RestClient.builder().baseUrl(partieUrl).build();
        this.joueurClient = RestClient.builder().baseUrl(joueurUrl).build();
    }

    /** Récupère toutes les parties depuis partie-service */
    public List<PartieDTO> getAllParties() {
        return partieClient.get()
                .uri("/parties")
                .retrieve()
                .body(new ParameterizedTypeReference<List<PartieDTO>>() {});
    }

    /** Stats détaillées d'un joueur */
    public StatJoueurDTO getStatJoueur(Long joueurId) {
        List<PartieDTO> parties = partieClient.get()
                .uri("/parties/joueur/" + joueurId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PartieDTO>>() {});

        JoueurDTO joueur = joueurClient.get()
                .uri("/joueurs/" + joueurId)
                .retrieve()
                .body(JoueurDTO.class);

        long gagnees = parties.stream().filter(p -> "GAGNEE".equals(p.getStatut())).count();
        long perdues = parties.stream().filter(p -> "PERDUE".equals(p.getStatut())).count();
        double taux = parties.isEmpty() ? 0 : (double) gagnees / parties.size() * 100;

        return new StatJoueurDTO(
                joueurId,
                joueur != null ? joueur.getPseudonyme() : "Inconnu",
                parties.size(),
                gagnees,
                perdues,
                Math.round(taux * 10.0) / 10.0
        );
    }

    /** Classement global : joueurs triés par taux de victoire */
    public List<StatJoueurDTO> getClassement() {
        List<PartieDTO> parties = getAllParties();

        // Grouper les parties par joueurId
        Map<Long, List<PartieDTO>> parJoueur = parties.stream()
                .collect(Collectors.groupingBy(PartieDTO::getJoueurId));

        List<StatJoueurDTO> classement = new ArrayList<>();

        for (Map.Entry<Long, List<PartieDTO>> entry : parJoueur.entrySet()) {
            Long joueurId = entry.getKey();
            List<PartieDTO> partiesJoueur = entry.getValue();

            // Récupérer le pseudo du joueur
            String pseudo;
            try {
                JoueurDTO joueur = joueurClient.get()
                        .uri("/joueurs/" + joueurId)
                        .retrieve()
                        .body(JoueurDTO.class);
                pseudo = joueur != null ? joueur.getPseudonyme() : "Inconnu";
            } catch (Exception e) {
                pseudo = "Joueur#" + joueurId;
            }

            long gagnees = partiesJoueur.stream().filter(p -> "GAGNEE".equals(p.getStatut())).count();
            long perdues = partiesJoueur.stream().filter(p -> "PERDUE".equals(p.getStatut())).count();
            double taux = (double) gagnees / partiesJoueur.size() * 100;

            classement.add(new StatJoueurDTO(
                    joueurId, pseudo,
                    partiesJoueur.size(), gagnees, perdues,
                    Math.round(taux * 10.0) / 10.0
            ));
        }

        // Trier par taux de victoire décroissant
        classement.sort(Comparator.comparingDouble(StatJoueurDTO::getTauxVictoire).reversed());
        return classement;
    }
}
