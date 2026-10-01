# Task API

Petite API REST de gestion de tâches développée avec **Java 17+**, **Spring Boot 4.1.1**, **Spring Data JPA** et **H2**.

Le projet a été réalisé dans le cadre d'un test technique Java/Spring Boot. Il se concentre uniquement sur l'API backend : aucune interface frontend, authentification ou fonctionnalité de déploiement n'est incluse.

## Fonctionnalités

* Création d'une tâche
* Liste de toutes les tâches
* Filtrage des tâches par statut
* Modification du statut d'une tâche
* Validation du titre
* Gestion des transitions de statut
* Gestion des erreurs HTTP
* Persistance avec H2
* Tests automatisés

### Statuts disponibles

```text
TODO
IN_PROGRESS
DONE
```

Les transitions autorisées sont :

```text
TODO → IN_PROGRESS → DONE
```

Toute autre transition est refusée avec `409 Conflict`.

---

## Technologies

* **Java 17**
* **Spring Boot 4.1.1**
* **Spring WebMVC**
* **Spring Data JPA**
* **Hibernate**
* **H2 Database**
* **Bean Validation**
* **JUnit / Spring MockMvc**
* **Maven Wrapper**

---

## Prérequis

* Java 17 ou version supérieure
* Aucun Maven installé globalement n'est nécessaire : le projet utilise le **Maven Wrapper**

Vérifier Java :

```bash
java -version
```

---

## Installation

Cloner le projet puis accéder au dossier :

```bash
git clone https://github.com/bahalee/task-api.git
cd task-api
```

Le projet peut également être ouvert directement dans IntelliJ IDEA.

---

## Lancer l'application

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

L'API sera disponible sur :

```text
http://localhost:8080
```

La base de données utilisée est une base H2 en mémoire :

```text
jdbc:h2:mem:taskdb
```

Les données sont donc réinitialisées lorsque l'application est arrêtée.

---

## Exécuter les tests

### Windows

```powershell
.\mvnw.cmd test
```

### Linux / macOS

```bash
./mvnw test
```

Le projet contient actuellement **11 tests automatisés** couvrant notamment :

* création valide 
* validation du titre 
* transitions autorisées 
* transition interdite 
* tâche inexistante 
* récupération des tâches 
* filtrage par statut 
* statut inconnu

Dernière exécution :

```text
Tests run: 11
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

---

# API

## 1. Créer une tâche

### `POST /tasks`

### Requête

```http
POST /tasks
Content-Type: application/json
```

```json
{
  "title": "Prepare technical test",
  "description": "Java Spring Boot API"
}
```

### Réponse `201 Created`

```json
{
  "title": "Prepare technical test",
  "description": "Java Spring Boot API",
  "id": 1,
  "status": "TODO"
}
```

### Règles

* Le titre est obligatoire.
* Le titre est trimé avant validation métier.
* Après `trim()`, le titre doit contenir entre **1 et 120 caractères**.
* La description est facultative.
* Le statut initial est toujours `TODO`.

---

## 2. Lister les tâches

### `GET /tasks`

```http
GET /tasks
```

### Réponse `200 OK`

```json
[
  {
    "title": "Prepare technical test",
    "description": "Java Spring Boot API",
    "id": 1,
    "status": "TODO"
  }
]
```

---

## 3. Filtrer par statut

### `GET /tasks?status={status}`

Exemple :

```http
GET /tasks?status=TODO
```

Statuts acceptés :

```text
TODO
IN_PROGRESS
DONE
```

Un statut inconnu retourne :

```text
400 Bad Request
```

---

## 4. Modifier le statut

### `PATCH /tasks/{id}/status`

Exemple :

```http
PATCH /tasks/1/status
Content-Type: application/json
```

```json
{
  "status": "IN_PROGRESS"
}
```

### Réponse `200 OK`

```json
{
  "title": "Prepare technical test",
  "description": "Java Spring Boot API",
  "id": 1,
  "status": "IN_PROGRESS"
}
```

### Transitions autorisées

```text
TODO → IN_PROGRESS
IN_PROGRESS → DONE
```

### Transition interdite

Par exemple :

```text
DONE → TODO
```

retourne :

```text
409 Conflict
```

avec :

```json
{
  "error": "Invalid status transition"
}
```

### Tâche inexistante

Une tâche dont l'identifiant n'existe pas retourne :

```text
404 Not Found
```

Exemple :

```json
{
  "error": "Task with id 999 not found"
}
```

---

# Gestion des erreurs

L'API utilise les codes HTTP suivants :

| Situation            |              HTTP |
| -------------------- | ----------------: |
| Création réussie     |     `201 Created` |
| Requête valide       |          `200 OK` |
| Titre invalide       | `400 Bad Request` |
| Statut inconnu       | `400 Bad Request` |
| Tâche inexistante    |   `404 Not Found` |
| Transition interdite |    `409 Conflict` |

Les erreurs métier sont centralisées dans `GlobalExceptionHandler`.

---

# Architecture

Le projet suit une séparation simple en couches :

```text
src/
├── main/
│   ├── java/com/baha/taskapi/
│   │   ├── controller/
│   │   │   └── TaskController.java
│   │   ├── dto/
│   │   │   └── CreateTaskRequest.java
│   │   ├── entity/
│   │   │   └── Task.java
│   │   ├── enums/
│   │   │   └── TaskStatus.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── InvalidTransitionException.java
│   │   │   └── TaskNotFoundException.java
│   │   ├── repository/
│   │   │   └── TaskRepository.java
│   │   ├── service/
│   │   │   └── TaskService.java
│   │   └── TaskApiApplication.java
│   └── resources/
│       └── application.properties
│
└── test/
    └── java/com/baha/taskapi/
        ├── TaskApiApplicationTests.java
        └── controller/
            └── TaskControllerTest.java
```

### Responsabilités

* **Controller** : exposition des endpoints REST et mapping des requêtes HTTP.
* **Service** : règles métier et transitions de statut.
* **Repository** : accès aux données via Spring Data JPA.
* **Entity** : représentation persistée d'une tâche.
* **DTO** : données reçues lors de la création.
* **Exception handler** : conversion des exceptions en réponses HTTP.

---

# Base de données

Le projet utilise **H2 en mémoire** afin de rester simple et facilement exécutable.

Configuration principale :

```properties
spring.datasource.url=jdbc:h2:mem:taskdb
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
```

La base est créée automatiquement au démarrage de l'application et supprimée à son arrêt.

La console H2 est également disponible à :

```text
http://localhost:8080/h2-console
```

---

# Tests automatisés

Les tests utilisent **Spring Boot + MockMvc**.

Les principaux scénarios testés sont :

1. création d'une tâche valide ;
2. rejet d'un titre invalide ;
3. transition interdite ;
4. transition `TODO → IN_PROGRESS` ;
5. transition `IN_PROGRESS → DONE` ;
6. tâche inexistante ;
7. récupération de toutes les tâches ;
8. filtrage par statut ;
9. statut invalide sur `GET` ;
10. statut invalide sur `PATCH` ;
11. démarrage du contexte Spring.

Résultat de la dernière exécution :

```text
Tests run: 11
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

---

# Temps passé

**Temps total approximatif : 2 heures.**

Le temps a principalement été consacré à :

* mise en place du projet Spring Boot 
* implémentation de l'API 
* implémentation des règles métier 
* gestion des erreurs 
* écriture des tests 
* vérification manuelle des endpoints 
* documentation

---

# Limites et choix

Le périmètre a volontairement été limité aux exigences du test.

### Non inclus

* Frontend
* Authentification / autorisation
* Déploiement
* Base de données externe
* Pagination
* Recherche avancée
* Gestion des utilisateurs
* Documentation OpenAPI / Swagger
* Gestion de concurrence avancée

### H2 en mémoire

H2 a été choisi conformément au sujet et pour faciliter l'exécution locale sans configuration externe.

### API volontairement simple

L'objectif est de garder une architecture lisible et facilement modifiable dans le temps imparti.

---

# Utilisation de la documentation et de l'IA

La documentation officielle et un assistant IA ont été utilisés comme **supports de développement et de vérification**.

L'IA a notamment été utilisée pour :

* clarifier certains points de configuration Spring Boot 
* vérifier des choix d'API et de tests 
* aider à identifier et corriger des problèmes de configuration liés à Spring Boot 4 
* relire certaines parties du code 
* structurer la documentation.

Le code a été exécuté, testé et vérifié localement, et je suis en mesure d'expliquer les choix d'implémentation et les règles métier.

---

# Auteur

**Baha Kebaili**

Full-Stack Web Developer

Tunisia

Technologies principales : Java, Spring Boot, React, Angular, Laravel, Node.js, SQL/MongoDB.
