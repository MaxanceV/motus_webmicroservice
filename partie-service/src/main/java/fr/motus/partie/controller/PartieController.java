package fr.motus.partie.controller;

import fr.motus.partie.model.Partie;
import fr.motus.partie.model.Tentative;
import fr.motus.partie.service.PartieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartieController {

    private final PartieService service;

    /** Lister toutes les parties (admin) */
    @GetMapping
    public List<Partie> getAll() {
        return service.findAll();
    }

    /** Récupérer une partie par id */
    @GetMapping("/{id}")
    public Partie getById(@PathVariable Long id) {
        return service.findById(id);
    }

    /** Historique des parties d'un joueur */
    @GetMapping("/joueur/{joueurId}")
    public List<Partie> getByJoueur(@PathVariable Long joueurId) {
        return service.findByJoueur(joueurId);
    }

    /** Lister les tentatives d'une partie */
    @GetMapping("/{id}/tentatives")
    public List<Tentative> getTentatives(@PathVariable Long id) {
        return service.getTentatives(id);
    }

    /** Indice : première lettre du mot mystère */
    @GetMapping("/{id}/premiere-lettre")
    public Map<String, String> getPremiereLettre(@PathVariable Long id) {
        Partie partie = service.findById(id);
        String lettre = partie.getMotMystere() != null
                ? String.valueOf(partie.getMotMystere().charAt(0))
                : "?";
        return Map.of("lettre", lettre);
    }

    /**
     * Créer une nouvelle partie
     * Body : { "joueurId": 1, "longueurMot": 6 }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Partie nouvellePartie(@RequestBody Map<String, Object> body) {
        Long joueurId = Long.valueOf(body.get("joueurId").toString());
        int longueur = body.containsKey("longueurMot")
                ? Integer.parseInt(body.get("longueurMot").toString())
                : 6;
        return service.nouvellePartie(joueurId, longueur);
    }

    /**
     * Soumettre une tentative
     * Body : { "mot": "MAISON" }
     */
    @PostMapping("/{id}/tentatives")
    @ResponseStatus(HttpStatus.CREATED)
    public Tentative soumettreTentative(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String mot = body.get("mot");
        return service.soumettreTentative(id, mot);
    }
}
