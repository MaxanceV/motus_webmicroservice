package fr.motus.dictionnaire.controller;

import fr.motus.dictionnaire.model.Mot;
import fr.motus.dictionnaire.service.DictionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mots")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DictionnaireController {

    private final DictionnaireService service;

    @GetMapping
    public List<Mot> getAll() {
        return service.findAll();
    }

    @GetMapping("/valider")
    public Map<String, Object> valider(@RequestParam String mot) {
        boolean valide = service.valider(mot);
        return Map.of("mot", mot.toUpperCase(), "valide", valide);
    }

    @GetMapping("/aleatoire")
    public Map<String, String> aleatoire(@RequestParam(defaultValue = "6") int longueur) {
        return Map.of("mot", service.motAleatoire(longueur));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mot add(@RequestBody Mot mot) {
        return service.add(mot);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
