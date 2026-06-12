package fr.motus.partie.service;

import fr.motus.partie.model.*;
import fr.motus.partie.repository.PartieRepository;
import fr.motus.partie.repository.TentativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PartieService {

    private final PartieRepository partieRepo;
    private final TentativeRepository tentativeRepo;
    private final RestClient dictionnaireClient;

    public PartieService(
            PartieRepository partieRepo,
            TentativeRepository tentativeRepo,
            @Value("${dictionnaire.service.url}") String dictionnaireUrl) {
        this.partieRepo = partieRepo;
        this.tentativeRepo = tentativeRepo;
        this.dictionnaireClient = RestClient.builder().baseUrl(dictionnaireUrl).build();
    }

    // ──────────────────────────────────────────────
    // Créer une nouvelle partie
    // ──────────────────────────────────────────────
    public Partie nouvellePartie(Long joueurId, int longueurMot) {
        // Appel au dictionnaire-service pour obtenir un mot aléatoire
        @SuppressWarnings("unchecked")
        Map<String, String> response = dictionnaireClient.get()
                .uri("/mots/aleatoire?longueur=" + longueurMot)
                .retrieve()
                .body(Map.class);

        String motMystere = response.get("mot");

        Partie partie = new Partie();
        partie.setJoueurId(joueurId);
        partie.setMotMystere(motMystere);
        partie.setNombreEssaisMax(6);
        return partieRepo.save(partie);
    }

    // ──────────────────────────────────────────────
    // Soumettre une tentative
    // ──────────────────────────────────────────────
    public Tentative soumettreTentative(Long partieId, String motPropose) {
        Partie partie = findById(partieId);

        if (partie.getStatut() != StatutPartie.EN_COURS) {
            throw new RuntimeException("Partie terminée (statut : " + partie.getStatut() + ")");
        }

        // Valider que le mot existe dans le dictionnaire
        @SuppressWarnings("unchecked")
        Map<String, Object> validation = dictionnaireClient.get()
                .uri("/mots/valider?mot=" + motPropose)
                .retrieve()
                .body(Map.class);

        if (!(Boolean) validation.get("valide")) {
            throw new RuntimeException("Mot invalide : " + motPropose + " n'est pas dans le dictionnaire");
        }

        int numeroEssai = tentativeRepo.countByPartieId(partieId) + 1;
        String motMajuscule = motPropose.toUpperCase();
        String motMystere = partie.getMotMystere();

        // Calculer le résultat lettre par lettre
        List<ResultatLettre> resultats = calculerResultat(motMajuscule, motMystere);

        Tentative tentative = new Tentative();
        tentative.setNumero(numeroEssai);
        tentative.setMotPropose(motMajuscule);
        tentative.setPartie(partie);

        // Lier les résultats à la tentative
        for (ResultatLettre r : resultats) {
            r.setTentative(tentative);
        }
        tentative.setResultats(resultats);

        tentativeRepo.save(tentative);

        // Vérifier victoire / défaite
        boolean gagne = resultats.stream().allMatch(r -> r.getEtat() == EtatLettre.BIEN_PLACEE);
        if (gagne) {
            partie.setStatut(StatutPartie.GAGNEE);
            partie.setDateFin(LocalDateTime.now());
            partieRepo.save(partie);
        } else if (numeroEssai >= partie.getNombreEssaisMax()) {
            partie.setStatut(StatutPartie.PERDUE);
            partie.setDateFin(LocalDateTime.now());
            partieRepo.save(partie);
        }

        return tentative;
    }

    // ──────────────────────────────────────────────
    // Algorithme Motus : calcul du résultat
    // ──────────────────────────────────────────────
    private List<ResultatLettre> calculerResultat(String propose, String mystere) {
        int longueur = Math.min(propose.length(), mystere.length());
        List<ResultatLettre> resultats = new ArrayList<>();
        boolean[] utiliseMystere = new boolean[longueur];
        boolean[] utilisePropose = new boolean[longueur];

        // 1er passage : BIEN_PLACEE
        for (int i = 0; i < longueur; i++) {
            ResultatLettre r = new ResultatLettre();
            r.setPosition(i);
            r.setLettre(propose.charAt(i));
            if (propose.charAt(i) == mystere.charAt(i)) {
                r.setEtat(EtatLettre.BIEN_PLACEE);
                utiliseMystere[i] = true;
                utilisePropose[i] = true;
            }
            resultats.add(r);
        }

        // 2ème passage : MAL_PLACEE ou ABSENTE
        for (int i = 0; i < longueur; i++) {
            if (utilisePropose[i]) continue;
            boolean found = false;
            for (int j = 0; j < longueur; j++) {
                if (!utiliseMystere[j] && propose.charAt(i) == mystere.charAt(j)) {
                    resultats.get(i).setEtat(EtatLettre.MAL_PLACEE);
                    utiliseMystere[j] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                resultats.get(i).setEtat(EtatLettre.ABSENTE);
            }
        }

        return resultats;
    }

    // ──────────────────────────────────────────────
    // Accesseurs
    // ──────────────────────────────────────────────
    public Partie findById(Long id) {
        return partieRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Partie non trouvée : " + id));
    }

    public List<Partie> findAll() {
        return partieRepo.findAll();
    }

    public List<Partie> findByJoueur(Long joueurId) {
        return partieRepo.findByJoueurId(joueurId);
    }

    public List<Tentative> getTentatives(Long partieId) {
        return tentativeRepo.findByPartieIdOrderByNumero(partieId);
    }
}
