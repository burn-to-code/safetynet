package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour gérer les opérations CRUD sur les personnes.
 */
@Slf4j
@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    /**
     * Constructeur avec injection du service PersonService.
     *
     * @param personService service métier pour gérer les personnes.
     */
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Ajoute une nouvelle personne.
     *
     * @param person La personne à ajouter (envoyée dans le corps de la requête).
     * @return La personne créée avec un code HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<?> addPerson(@RequestBody Person person) {
        log.info("Requête POST /person reçue avec payload : {}", person);
        try {
            personService.addPerson(person);
            log.info("Personne créée avec succès : {}", person);
            return ResponseEntity.status(HttpStatus.CREATED).body(person);
        } catch (IllegalStateException e) {
            log.error("Erreur lors de la création de la personne : {}", person.getId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Met à jour une personne existante.
     *
     * @param person La personne avec les nouvelles données (envoyée dans le corps de la requête).
     * @return La personne mise à jour avec un code HTTP 200 (OK).
     */
    @PutMapping()
    public ResponseEntity<?> updatePerson(@RequestBody Person person ) {
        log.info("Requête PUT /person reçue avec payload : {}", person);
        try {
            personService.updatePerson(person);
            log.info("Personne mise à jour avec succès : {}", person);
            return ResponseEntity.ok(person);
        } catch (IllegalStateException e) {
            log.error("Erreur lors de la mise à jour de la personne : {}", person.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    /**
     * Supprime une personne par son prénom et nom.
     *
     * @param firstName Prénom de la personne à supprimer (passé en paramètre de requête).
     * @param lastName  Nom de la personne à supprimer (passé en paramètre de requête).
     * @return Un code HTTP 204 (No Content) indiquant la suppression réussie.
     */
    @DeleteMapping()
    // /person?firstName=xxx&lastName=YYY
    public ResponseEntity<?> deletePerson(@RequestParam  String firstName, @RequestParam  String lastName) {
        log.info("Requête DELETE /person reçue pour {} {}", firstName, lastName);
        try {
            personService.removePerson(firstName, lastName);
            log.info("Personne supprimée avec succès : {} {}", firstName, lastName);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.error("Erreur lors de la suppression de la personne : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
