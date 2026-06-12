package fr.motus.dictionnaire.service;

import fr.motus.dictionnaire.model.Mot;
import fr.motus.dictionnaire.repository.MotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionnaireService {

    private final MotRepository repo;

    public boolean valider(String mot) {
        return repo.existsByValeurIgnoreCase(mot.trim());
    }

    public String motAleatoire(int longueur) {
        return repo.findRandomByLongueur(longueur)
                .map(Mot::getValeur)
                .orElseThrow(() -> new RuntimeException("Aucun mot de longueur " + longueur + " dans le dictionnaire"));
    }

    public List<Mot> findAll() {
        return repo.findAll();
    }

    public Mot add(Mot mot) {
        if (repo.existsByValeurIgnoreCase(mot.getValeur())) {
            throw new RuntimeException("Mot déjà présent : " + mot.getValeur());
        }
        return repo.save(mot);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
