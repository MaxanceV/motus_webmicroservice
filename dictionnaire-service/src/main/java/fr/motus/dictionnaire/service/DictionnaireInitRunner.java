package fr.motus.dictionnaire.service;

import fr.motus.dictionnaire.model.Mot;
import fr.motus.dictionnaire.repository.MotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Au démarrage, si le dictionnaire a moins de 50 mots,
 * tente de le compléter depuis une liste publique en ligne.
 * En cas d'échec réseau, le data.sql local prend le relais.
 */
@Component
@RequiredArgsConstructor
public class DictionnaireInitRunner implements ApplicationRunner {

    private final MotRepository repo;

    private static final String WORD_LIST_URL =
            "https://raw.githubusercontent.com/lorenbrichter/Words/master/Words/fr.txt";

    @Override
    public void run(ApplicationArguments args) {
        long count = repo.count();
        if (count >= 50) {
            System.out.println("📖 Dictionnaire OK — " + count + " mots en base.");
            return;
        }

        System.out.println("🌐 Chargement du dictionnaire depuis internet...");

        try {
            RestClient client = RestClient.create();
            String content = client.get()
                    .uri(WORD_LIST_URL)
                    .retrieve()
                    .body(String.class);

            if (content == null || content.isBlank()) {
                System.out.println("⚠️  Réponse vide — utilisation du dictionnaire local uniquement.");
                return;
            }

            String[] lines = content.split("\n");
            int added = 0;

            for (String line : lines) {
                String mot = line.trim().toUpperCase();
                // Garder uniquement les mots de 5 à 7 lettres, sans accents ni caractères spéciaux
                if (mot.matches("[A-Z]{5,7}") && !repo.existsByValeurIgnoreCase(mot)) {
                    Mot m = new Mot();
                    m.setValeur(mot);
                    repo.save(m);
                    added++;
                }
            }

            System.out.println("✅ " + added + " mots ajoutés depuis internet. Total : " + repo.count());

        } catch (Exception e) {
            System.out.println("⚠️  Impossible de joindre internet : " + e.getMessage());
            System.out.println("   Le dictionnaire local (data.sql) sera utilisé.");
        }
    }
}
