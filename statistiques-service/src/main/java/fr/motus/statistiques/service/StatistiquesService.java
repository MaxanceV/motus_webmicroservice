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
        // ParameterizedTypeReference est nécessaire pour désérialiser une List<PartieDTO>.
        // Sans ça, Java "efface" le type générique à l'exécution (type erasure) et
        // RestClient ne saurait pas dans quoi convertir le JSON.
        // En gros : on lui dit explicitement "c'est une List<PartieDTO>".
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
        // Taux de victoire en % arrondi à 1 décimale
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

    /** Classement global : joueurs triés par taux de victoire décroissant */
    public List<StatJoueurDTO> getClassement() {
        List<PartieDTO> parties = getAllParties();

        // Collectors.groupingBy regroupe les parties par joueurId dans une Map.
        // Résultat : { 1L -> [partie1, partie2], 2L -> [partie3], ... }
        // C'est l'équivalent d'un GROUP BY SQL, mais en Java Stream.
        Map<Long, List<PartieDTO>> parJoueur = parties.stream()
                .collect(Collectors.groupingBy(PartieDTO::getJoueurId));

        List<StatJoueurDTO> classement = new ArrayList<>();

        for (Map.Entry<Long, List<PartieDTO>> entry : parJoueur.entrySet()) {
            Long joueurId = entry.getKey();
            List<PartieDTO> partiesJoueur = entry.getValue();

            // On récupère le pseudo via joueur-service pour chaque joueur distinct.
            // Note : si joueur-service est down, on met un nom de secours — le try/catch
            // évite que tout le classement plante à cause d'un seul joueur introuvable.
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

        // Tri par taux de victoire décroissant (.reversed() inverse l'ordre naturel)
        classement.sort(Comparator.comparingDouble(StatJoueurDTO::getTauxVictoire).reversed());
        return classement;
    }
}
