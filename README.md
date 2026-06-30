# Motus — Projet Web Microservices (MIAGE M2)

**Binôme :** Maxance & Ferdinand Martin Lavigne  
**Cours :** M. Menceur — Architecture Web & Microservices

---

## Prérequis

| Outil | Version minimale | Vérification |
|-------|-----------------|--------------|
| Java (JDK) | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker Desktop | Dernière | `docker -v` |
| Git | Quelconque | `git --version` |

---

## Cloner le projet
```bash
git clone <url-du-repo>
cd motus_webmicroservice
```

---

## Lancer le projet

### Étape 1 — Ouvre Docker Desktop

Lance **Docker Desktop** et attends que l'icône soit stable dans la barre des tâches. C'est tout ce qu'il y a à faire avec Docker — ne ferme pas Docker, ne tape rien dedans.

> Si Docker tourne déjà, pas besoin de le redémarrer. Le script est idempotent.

### Étape 2 — Lance le script

**Choisis UNE des méthodes selon d'où tu viens :**

#### ✅ Méthode A — Double-clic (recommandé, aucun terminal requis)
Double-clique sur `start-motus.bat` dans l'explorateur Windows. Pur CMD, pas besoin de PowerShell.

#### ✅ Méthode B — Git Bash ou CMD
```bash
start-motus.bat
```

#### ✅ Méthode C — PowerShell
Ouvre PowerShell dans le dossier du projet (clic droit → "Ouvrir dans Terminal"), puis :
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\start-motus.ps1
```

### Ce que fait le script

5 fenêtres `cmd` s'ouvrent, une par service :

| Fenêtre | Service | Port |
|---------|---------|------|
| `[8082] dictionnaire-service` | Dictionnaire | 8082 |
| `[8081] joueur-service` | Joueurs | 8081 |
| `[8083] partie-service` | Logique du jeu | 8083 |
| `[8084] statistiques-service` | Stats | 8084 |
| `[8080] api-gateway` | Point d'entrée unique | 8080 |

**Attends que chaque fenêtre affiche :**
```
Started XxxServiceApplication in X.XXX seconds
```
⏱️ Durée : ~2 min. La première fois (Maven télécharge tout) : ~5 min.

### Étape 3 — Ouvre le site

Double-clique sur `frontend/index.html` dans l'explorateur Windows (ou glisse-le dans Chrome/Firefox).

Le jeu s'affiche directement — pas de serveur web nécessaire.

---

## Jouer

1. **Inscris-toi** → pseudo, email, mot de passe
2. **Connecte-toi** → entre ton pseudo
3. **Choisis la longueur** du mot (5, 6 ou 7 lettres)
4. **Clique "Nouvelle partie"**
5. **Tape un mot** + Entrée

Les cases se colorient comme au Motus :
- 🔴 **Rouge** = bonne lettre, bonne position
- 🟡 **Jaune** = bonne lettre, mauvaise position  
- ⬜ **Gris** = lettre absente

La **première lettre** du mot mystère est toujours affichée.

---

## Tester via API (optionnel)

```bash
# Créer un joueur (via Gateway)
curl -X POST http://localhost:8080/joueurs \
  -H "Content-Type: application/json" \
  -d '{"pseudonyme":"Ferdinand","email":"ferd@test.fr","motDePasse":"1234"}'

# Lancer une partie (mot de 6 lettres)
curl -X POST http://localhost:8080/parties \
  -H "Content-Type: application/json" \
  -d '{"joueurId":1,"longueurMot":6}'

# Soumettre un essai
curl -X POST http://localhost:8080/parties/1/tentatives \
  -H "Content-Type: application/json" \
  -d '{"mot":"MAISON"}'

# Voir le classement
curl http://localhost:8080/statistiques/classement
```

---

## Dictionnaire

- **Local** : ~200 mots (5–7 lettres) chargés depuis `data.sql` au démarrage
- **Internet** : au premier démarrage, le service tente de télécharger une liste publique de mots français et en importe plusieurs milliers. Si pas de connexion, le local suffit.

> ⚠️ Les mots avec accents (É, È, Â...) ne sont pas supportés. Exemples valides : MAISON, JARDIN, SOLEIL, CHANCE, CHEVAL, VOITURE...

---

## Problèmes fréquents

**"command not found" sur `Set-ExecutionPolicy` ou `start-motus.ps1`**  
→ Tu es dans Git Bash. Utilise : `powershell -ExecutionPolicy Bypass -File ./start-motus.ps1`  
→ Ou double-clique sur `start-motus.bat`

**Les services ne démarrent pas**  
→ Vérifie que Docker Desktop est ouvert (`docker ps` doit répondre)

**"Mot non reconnu"**  
→ Le mot n'est pas dans le dico, ou il contient un accent. Essaie MAISON, JARDIN, SOLEIL.

**"Port déjà utilisé"**  
→ Ferme les fenêtres cmd précédentes et relance

**Erreur de connexion à la DB**  
→ Attends 10 secondes que PostgreSQL soit prêt, puis relance le service concerné

---

## Architecture

```
frontend/index.html
       │
       ▼
api-gateway      :8080  (Spring Cloud Gateway)
       │
       ├──▶ joueur-service       :8081  (PostgreSQL :5432)
       ├──▶ dictionnaire-service :8082  (PostgreSQL :5433)
       ├──▶ partie-service       :8083  (PostgreSQL :5434)
       └──▶ statistiques-service :8084  (pas de DB propre)
```

---

## Endpoints

### joueur-service (8081)
| Méthode | URL | Corps |
|---------|-----|-------|
| POST | `/joueurs` | `{pseudonyme, email, motDePasse}` |
| GET | `/joueurs` | — |
| GET | `/joueurs/{id}` | — |
| PUT | `/joueurs/{id}` | champs à modifier |
| DELETE | `/joueurs/{id}` | — |

### dictionnaire-service (8082)
| Méthode | URL | |
|---------|-----|-|
| GET | `/mots/valider?mot=X` | true/false |
| GET | `/mots/aleatoire?longueur=6` | mot aléatoire |
| GET | `/mots` | liste complète |
| POST | `/mots` | `{valeur}` |

### partie-service (8083)
| Méthode | URL | Corps |
|---------|-----|-------|
| POST | `/parties` | `{joueurId, longueurMot}` |
| GET | `/parties/{id}` | — |
| GET | `/parties/joueur/{joueurId}` | — |
| POST | `/parties/{id}/tentatives` | `{mot}` |
| GET | `/parties/{id}/tentatives` | — |
| GET | `/parties/{id}/premiere-lettre` | — |

### statistiques-service (8084)
| Méthode | URL | |
|---------|-----|-|
| GET | `/statistiques/joueur/{joueurId}` | stats d'un joueur |
| GET | `/statistiques/classement` | classement global |
| GET | `/statistiques/parties` | toutes les parties (admin) |

---

## Technologies

- Java 21 · Spring Boot 4.1.0 · Spring Data JPA · PostgreSQL
- Spring Cloud Gateway MVC · RestClient · Docker / docker-compose
- Lombok · HTML/CSS/JS vanilla (frontend)
