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
- **Integration** : Tests d'intégrations des controllers (Endpoints)

---

## Technologies utilisées

- Java 21+
- Spring Boot 3.5.0
- Lombok
- Spring Web MVC
- JSON
- Maven : 3.9.9

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
    - GET `/fire?address=xxx` : Récupère une liste de personnes (nom, prénom, adresse et téléphone + medicament et allergies)
    - GET `/flood/stations?stationNumber=xxx?stationNumber=yyy` : Récupère une liste de {@link FloodResponseDTO}, chaque élément contenant :
  *         <ul>
  *             <li>l'adresse d'un foyer,</li>
  *             <li>la liste des occupants du foyer, avec leurs informations personnelles
  *                 et médicales</li>
  *         </ul>
    - GET `/personInfosLastName?lastName=xxx` : return une liste de personnes (le nom, l'adresse, l'âge, l'adresse mail et les antécédents
      médicaux (médicaments, posologie et allergies)) de chaque habitant avec ce nom de famille
    - GET  `/communityEmail?city=xxx`: Retourne Une liste des emails de tous les habitants d'une ville

---

## Installation & utilisation

1. Cloner ce dépôt
2. Compiler et lancer l’application via Maven ou votre IDE (si bien configuré)
3. Utiliser un client REST (Postman, curl) pour tester les endpoints
4. Les modifications seront actualisées dans Data/Json.data.

---

## Test

1. Pour Lancer les tests : mvn test
2. Pour générer un rapport de test avec jacoco : mvn clean test ---> mvn jacoco:report ou mvn clean verify
3. Pour lancer les tests, et générés tous les rapports (jacoco et surefire) : mvn site

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

