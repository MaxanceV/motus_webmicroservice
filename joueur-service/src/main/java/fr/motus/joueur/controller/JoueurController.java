package fr.motus.joueur.controller;

import fr.motus.joueur.model.Joueur;
import fr.motus.joueur.service.JoueurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/joueurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JoueurController {

    private final JoueurService service;

    @GetMapping
    public List<Joueur> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Joueur getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/email/{email}")
    public Joueur getByEmail(@PathVariable String email) {
        return service.findByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Joueur inscription(@Valid @RequestBody Joueur joueur) {
        return service.inscription(joueur);
    }

    @PutMapping("/{id}")
    public Joueur update(@PathVariable Long id, @RequestBody Joueur joueur) {
        return service.update(id, joueur);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
