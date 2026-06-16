package fr.motus.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

/**
 * Gateway manuelle : reçoit toutes les requêtes sur :8080
 * et les route vers le bon microservice via RestClient.
 *
 * Routes :
 *   /joueurs/**       → joueur-service       :8081
 *   /mots/**          → dictionnaire-service  :8082
 *   /parties/**       → partie-service        :8083
 *   /statistiques/**  → statistiques-service  :8084
 */
@RestController
public class ProxyController {

    private final RestClient joueurClient;
    private final RestClient dictionnaireClient;
    private final RestClient partieClient;
    private final RestClient statistiquesClient;

    public ProxyController(
            @Value("${joueur.service.url}") String joueurUrl,
            @Value("${dictionnaire.service.url}") String dictionnaireUrl,
            @Value("${partie.service.url}") String partieUrl,
            @Value("${statistiques.service.url}") String statistiquesUrl) {
        this.joueurClient       = RestClient.builder().baseUrl(joueurUrl).build();
        this.dictionnaireClient = RestClient.builder().baseUrl(dictionnaireUrl).build();
        this.partieClient       = RestClient.builder().baseUrl(partieUrl).build();
        this.statistiquesClient = RestClient.builder().baseUrl(statistiquesUrl).build();
    }

    @RequestMapping("/joueurs/**")
    public ResponseEntity<byte[]> joueurs(HttpServletRequest req) throws IOException {
        return forward(joueurClient, req);
    }

    @RequestMapping("/mots/**")
    public ResponseEntity<byte[]> mots(HttpServletRequest req) throws IOException {
        return forward(dictionnaireClient, req);
    }

    @RequestMapping("/parties/**")
    public ResponseEntity<byte[]> parties(HttpServletRequest req) throws IOException {
        return forward(partieClient, req);
    }

    @RequestMapping("/statistiques/**")
    public ResponseEntity<byte[]> statistiques(HttpServletRequest req) throws IOException {
        return forward(statistiquesClient, req);
    }

    // ─────────────────────────────────────────────────────────────
    //  Cœur du proxy : lit la requête entrante et la forwarde
    //
    //  Principe du pattern "reverse proxy" :
    //    Client → Gateway (:8080) → Service cible (:8081-8084)
    //
    //  On recopie fidèlement la méthode HTTP (GET/POST/PUT/DELETE),
    //  l'URI complète avec les query params (?longueur=5 etc.),
    //  et le corps de la requête si présent (ex: JSON pour un POST).
    //  La réponse du service est retransmise telle quelle (byte[])
    //  pour ne pas perturber la sérialisation JSON.
    // ─────────────────────────────────────────────────────────────
    private ResponseEntity<byte[]> forward(RestClient client, HttpServletRequest req) throws IOException {
        // On reconstruit l'URI complète en ajoutant les query params si présents
        String uri    = req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
        HttpMethod method = HttpMethod.valueOf(req.getMethod());
        // readAllBytes() lit le body en entier (peut être vide pour un GET)
        byte[] body   = req.getInputStream().readAllBytes();

        try {
            if (body.length > 0) {
                // POST/PUT avec body JSON → on le transfère tel quel
                return client.method(method).uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toEntity(byte[].class);
            } else {
                // GET/DELETE sans body
                return client.method(method).uri(uri)
                        .retrieve()
                        .toEntity(byte[].class);
            }
        } catch (RestClientResponseException e) {
            // RestClientResponseException = le service a répondu mais avec 4xx ou 5xx.
            // On propage le code d'erreur original (400 Bad Request, 404 Not Found, etc.)
            // au lieu de retourner 200 ou 500 de façon trompeuse.
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            // Exception réseau (service down, timeout...) → 502 Bad Gateway
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\":\"Service indisponible : " + e.getMessage() + "\"}").getBytes());
        }
    }
}
