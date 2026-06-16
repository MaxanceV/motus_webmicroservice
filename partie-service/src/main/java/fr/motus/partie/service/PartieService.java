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

    // On injecte l'URL du dictionnaire-service depuis application.properties
    // et on crée un RestClient dessus — c'est Spring Boot 3.2+ (remplace RestTemplate)
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
        // Appel HTTP GET vers dictionnaire-service pour tirer un mot au hasard.
        // On reçoit du JSON genre { "mot": "MAISON" } qu'on mappe dans une Map<String, String>.
        // Le @SuppressWarnings("unchecked") sert juste à éviter un warning du compilateur
        // sur le cast générique — c'est cosmétique, rien de grave.
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

        // On refuse les tentatives si la partie est déjà terminée
        if (partie.getStatut() != StatutPartie.EN_COURS) {
            throw new RuntimeException("Partie terminée (statut : " + partie.getStatut() + ")");
        }

        // Validation du mot proposé : on appelle dictionnaire-service en HTTP.
        // La réponse est genre { "valide": true } ou { "valide": false }
        @SuppressWarnings("unchecked")
        Map<String, Object> validation = dictionnaireClient.get()
                .uri("/mots/valider?mot=" + motPropose)
                .retrieve()
                .body(Map.class);

        if (!(Boolean) validation.get("valide")) {
            throw new RuntimeException("Mot invalide : " + motPropose + " n'est pas dans le dictionnaire");
        }

        // countByPartieId donne le nombre de tentatives déjà faites — +1 pour le numéro actuel
        int numeroEssai = tentativeRepo.countByPartieId(partieId) + 1;
        String motMajuscule = motPropose.toUpperCase();
        String motMystere = partie.getMotMystere();

        // C'est ici que se passe la logique Motus — voir la méthode calculerResultat()
        List<ResultatLettre> resultats = calculerResultat(motMajuscule, motMystere);

        Tentative tentative = new Tentative();
        tentative.setNumero(numeroEssai);
        tentative.setMotPropose(motMajuscule);
        tentative.setPartie(partie);

        // On lie chaque ResultatLettre à la tentative avant de sauvegarder
        // (nécessaire pour que JPA comprenne la relation OneToMany)
        for (ResultatLettre r : resultats) {
            r.setTentative(tentative);
        }
        tentative.setResultats(resultats);

        tentativeRepo.save(tentative);

        // Victoire : toutes les lettres sont BIEN_PLACEE
        boolean gagne = resultats.stream().allMatch(r -> r.getEtat() == EtatLettre.BIEN_PLACEE);
        if (gagne) {
            partie.setStatut(StatutPartie.GAGNEE);
            partie.setDateFin(LocalDateTime.now());
            partieRepo.save(partie);
        } else if (numeroEssai >= partie.getNombreEssaisMax()) {
            // Défaite : on a utilisé tous les essais
            partie.setStatut(StatutPartie.PERDUE);
            partie.setDateFin(LocalDateTime.now());
            partieRepo.save(partie);
        }

        return tentative;
    }

    // ──────────────────────────────────────────────
    //  ALGORITHME MOTUS
    //  Principe : comparer lettre par lettre le mot proposé au mot mystère
    //  et attribuer un état à chaque lettre : BIEN_PLACEE, MAL_PLACEE, ABSENTE
    //
    //  Pourquoi deux passes ? Pour gérer les lettres en double correctement.
    //  Exemple : mot mystère = "LARME", proposé = "LALLE"
    //    La 1ère passe détecte L bien placé en position 0.
    //    La 2ème passe voit le 2ème L (position 2) : le seul L restant du mystère
    //    est déjà marqué utilisé → la lettre est ABSENTE, pas MAL_PLACEE.
    //  Sans les tableaux booléens, on surcompterait les lettres dupliquées.
    // ──────────────────────────────────────────────
    private List<ResultatLettre> calculerResultat(String propose, String mystere) {
        int longueur = Math.min(propose.length(), mystere.length());
        List<ResultatLettre> resultats = new ArrayList<>();

        // Ces deux tableaux servent à "consommer" les lettres au fur et à mesure
        // pour éviter de compter deux fois la même lettre du mot mystère
        boolean[] utiliseMystere = new boolean[longueur]; // lettre du mystère déjà matchée
        boolean[] utilisePropose = new boolean[longueur]; // lettre du proposé déjà traitée

        // ── PASSE 1 : on cherche les lettres BIEN_PLACEE (même position) ──
        for (int i = 0; i < longueur; i++) {
            ResultatLettre r = new ResultatLettre();
            r.setPosition(i);
            r.setLettre(propose.charAt(i));
            if (propose.charAt(i) == mystere.charAt(i)) {
                r.setEtat(EtatLettre.BIEN_PLACEE);
                utiliseMystere[i] = true; // cette lettre du mystère est "consommée"
                utilisePropose[i] = true; // cette lettre du proposé est "consommée"
            }
            resultats.add(r);
        }

        // ── PASSE 2 : pour les lettres pas encore traitées, chercher MAL_PLACEE ou ABSENTE ──
        for (int i = 0; i < longueur; i++) {
            if (utilisePropose[i]) continue; // déjà traité en passe 1

            boolean found = false;
            // On cherche si cette lettre existe ailleurs dans le mystère (non consommée)
            for (int j = 0; j < longueur; j++) {
                if (!utiliseMystere[j] && propose.charAt(i) == mystere.charAt(j)) {
                    resultats.get(i).setEtat(EtatLettre.MAL_PLACEE);
                    utiliseMystere[j] = true; // consommer cette occurrence du mystère
                    found = true;
                    break; // on s'arrête : une seule occurrence consommée par lettre
                }
            }
            if (!found) {
                resultats.get(i).setEtat(EtatLettre.ABSENTE);
            }
        }

        return resultats;
    }

    // ──────────────────────────────────────────────
    // Accesseurs simples
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
