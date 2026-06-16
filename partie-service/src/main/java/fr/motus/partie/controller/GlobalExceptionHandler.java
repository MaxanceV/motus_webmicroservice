package fr.motus.partie.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// @RestControllerAdvice : intercepte les exceptions levées dans TOUS les @RestController
// du service, sans avoir à écrire de try/catch dans chaque méthode.
// Principe : si une RuntimeException bubble jusqu'ici, on renvoie un 400 avec un JSON
// { "error": "message" } au lieu d'une stack trace HTML (qui serait incompréhensible pour le client).
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handle(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}
