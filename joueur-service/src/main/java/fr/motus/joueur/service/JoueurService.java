package fr.motus.joueur.service;

import fr.motus.joueur.model.Joueur;
import fr.motus.joueur.repository.JoueurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JoueurService {

    private final JoueurRepository repo;
    private final PasswordEncoder passwordEncoder;

    public List<Joueur> findAll() {
        return repo.findAll();
    }

    public Joueur findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé : " + id));
    }

    public Joueur findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé : " + email));
    }

    public Joueur inscription(Joueur joueur) {
        if (repo.existsByEmail(joueur.getEmail())) {
            throw new RuntimeException("Email déjà utilisé : " + joueur.getEmail());
        }
        // Hashage BCrypt avant persistance
        joueur.setMotDePasse(passwordEncoder.encode(joueur.getMotDePasse()));
        return repo.save(joueur);
    }

    public Joueur update(Long id, Joueur updates) {
        Joueur joueur = findById(id);
        joueur.setPseudonyme(updates.getPseudonyme());
        if (updates.getMotDePasse() != null && !updates.getMotDePasse().isBlank()) {
            joueur.setMotDePasse(passwordEncoder.encode(updates.getMotDePasse()));
        }
        return repo.save(joueur);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
