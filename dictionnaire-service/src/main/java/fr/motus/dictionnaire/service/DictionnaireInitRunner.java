package fr.motus.dictionnaire.service;

import fr.motus.dictionnaire.model.Mot;
import fr.motus.dictionnaire.repository.MotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Initialisation du dictionnaire au démarrage de l'application.
 *
 * ApplicationRunner est une interface Spring Boot : la méthode run() est appelée
 * automatiquement juste après que le contexte Spring soit complètement chargé.
 * C'est utile pour du code d'initialisation qui a besoin des beans (repo, services...)
 * et qui doit tourner une seule fois au lancement — ici, vérifier que le dico est rempli.
 *
 * Stratégie de fallback à deux niveaux :
 *   1. Si la base a déjà ≥ 50 mots (chargés par data.sql) → rien à faire
 *   2. Sinon → tentative de chargement depuis GitHub
 *   3. En cas d'échec réseau → on continue avec le dictionnaire local uniquement
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
            // data.sql a déjà été chargé par Spring Boot au démarrage → rien à faire
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
                // Regex [A-Z]{5,7} : uniquement des lettres majuscules sans accents, 5 à 7 caractères.
                // existsByValeurIgnoreCase évite les doublons si on relance le runner (idempotence).
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
