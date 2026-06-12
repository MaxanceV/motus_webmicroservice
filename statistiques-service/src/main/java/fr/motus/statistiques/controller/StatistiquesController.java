package fr.motus.statistiques.controller;

import fr.motus.statistiques.dto.PartieDTO;
import fr.motus.statistiques.dto.StatJoueurDTO;
import fr.motus.statistiques.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistiques")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatistiquesController {

    private final StatistiquesService service;

    /** Toutes les parties (pour admin) */
    @GetMapping("/parties")
    public List<PartieDTO> getAllParties() {
        return service.getAllParties();
    }

    /** Stats d'un joueur spécifique */
    @GetMapping("/joueur/{joueurId}")
    public StatJoueurDTO getStatJoueur(@PathVariable Long joueurId) {
        return service.getStatJoueur(joueurId);
    }

    /** Classement global des joueurs */
    @GetMapping("/classement")
    public List<StatJoueurDTO> getClassement() {
        return service.getClassement();
    }
}
