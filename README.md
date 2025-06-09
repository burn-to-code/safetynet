# SafetyNet - API de gestion des alertes et des informations de sécurité

## Description

SafetyNet est une application backend développée en Java avec Spring Boot, visant à gérer les informations relatives aux personnes, casernes de pompiers, dossiers médicaux et alertes diverses (enfants à protéger, numéros de téléphone liés à une station de pompiers, etc.).  
Ce service est conçu pour fournir des API REST robustes afin de gérer les données critiques pour la sécurité civile.

---

## Fonctionnalités principales

- Gestion CRUD des **Personnes**
- Gestion CRUD des **Casernes de pompiers** (FireStations)
- Gestion CRUD des **Dossiers médicaux** (MedicalRecords)
- Recherche d’enfants vivant à une adresse spécifique (ChildAlert)
- Recherche des numéros de téléphone liés à une station de pompiers (PhoneAlert)
- Récupération des personnes couvertes par une station de pompiers donnée

---

## Architecture du projet

- **Modèles (Model)** : Contiennent les entités métier et DTOs (ex: `Person`, `FireStation`, `MedicalRecord`, `ChildAlertDTO`, `PersonCoveredDTO`).
- **Repositories** : Interfaces d’accès aux données (non détaillées dans le code partagé, mais supposées présentes).
- **Services** : Logique métier principale, validation, gestion des entités.
- **Contrôleurs (Controllers)** : Exposent les API REST, réceptionnent et renvoient des données HTTP.
- **Logging** : Utilisation de Lombok (`@Slf4j`) pour la journalisation des actions importantes.
- **Validation simple** : Via Spring `Assert` dans les services.

---

## Technologies utilisées

- Java 21+
- Spring Boot
- Lombok
- Spring Web MVC
- JSON
- Maven/Gradle (selon configuration)

---

## Exemples d’API

- **Person**
    - POST `/person` : Ajouter une personne
    - PUT `/person` : Mettre à jour une personne
    - DELETE `/person?firstName=xxx&lastName=yyy` : Supprimer une personne

- **FireStation**
    - POST `/firestation` : Ajouter une caserne
    - PUT `/firestation` : Mettre à jour une caserne
    - DELETE `/firestation?address=xxx` : Supprimer une caserne
    - GET `/firestation?stationNumber=xxx` : Obtenir les personnes couvertes par une caserne

- **MedicalRecord**
    - POST `/medicalrecord` : Ajouter un dossier médical
    - PUT `/medicalrecord` : Mettre à jour un dossier médical
    - DELETE `/medicalrecord?firstName=xxx&lastName=yyy` : Supprimer un dossier médical

- **Alertes**
    - GET `/childAlert?address=xxx` : Liste des enfants à cette adresse
    - GET `/phoneAlert?fireStation=xxx` : Liste des numéros de téléphone liés à une station

---

## Installation & utilisation

1. Cloner ce dépôt
2. Configurer le fichier JSON dans `JSONDataStorageImpl.JAVA`
3. Compiler et lancer l’application via Maven
4. Utiliser un client REST (Postman, curl) pour tester les endpoints

---

## Bonnes pratiques mises en place

- Séparation claire entre couches Controller, Service et Repository
- Utilisation de DTOs pour exposer uniquement les données nécessaires
- Logging des opérations importantes
- Validation basique des entrées dans les services
- Usage des annotations Spring REST (`@RestController`, `@RequestMapping`, etc.)
- Documentation Javadoc ajoutée sur les services et contrôleurs

---

## Pistes d’amélioration recommandées

- **Gestion des erreurs améliorée** :  
  Créer des exceptions métier personnalisées et gérer les erreurs avec `@ControllerAdvice` pour des réponses HTTP plus précises (404, 409, etc.).

- **Validation des entrées** :  
  Utiliser les annotations de validation (`@NotNull`, `@Size`, `@Valid`) dans les modèles et dans les contrôleurs.

- **Tests automatisés** :  
  Ajouter des tests unitaires pour les services et tests d’intégration pour les contrôleurs.

- **Documentation API** :  
  Intégrer Swagger/OpenAPI pour une documentation interactive des API.

- **Sécurité** :  
  Ajouter une couche d’authentification/autorisation (ex: Spring Security) selon besoins.

---

## Auteur

Dylan Senasson - Développeur backend

---

## Licence

SafetyNet

---

