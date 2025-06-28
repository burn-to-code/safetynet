package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.exception.ConflictException;
import com.safetynet.AppSafetyNet.exception.NotFoundException;
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
     * Ajoute une nouvelle personne à la base de données.
     * <p>
     * Cette opération échoue si une personne avec le même prénom et nom existe déjà.
     * </p>
     *
     * @param person l'objet {@link Person} reçu dans le corps de la requête, à ajouter.
     * @return une réponse HTTP 201 (Created) contenant la personne ajoutée.
     * @throws ConflictException si la personne existe déjà.
     */
    @PostMapping
    public ResponseEntity<?> addPerson(@RequestBody Person person) {
        log.info("Requête POST /person reçue avec payload : {}", person);
        personService.addPerson(person);
        log.info("Personne créée avec succès : {}", person);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    /**
     * Met à jour une personne existante à partir de son prénom et nom.
     * <p>
     * Seules les informations de contact et d’adresse peuvent être modifiées.
     * </p>
     *
     * @param person l'objet {@link Person} contenant les données mises à jour.
     * @return une réponse HTTP 200 (OK) avec la personne mise à jour.
     * @throws NotFoundException si aucune personne correspondante n’est trouvée.
     */
    @PutMapping()
    public ResponseEntity<?> updatePerson(@RequestBody Person person ) {
        log.info("Requête PUT /person reçue avec payload : {}", person);
        personService.updatePerson(person);
        log.info("Personne mise à jour avec succès : {}", person);
        return ResponseEntity.ok(person);
    }

    /**
     * Supprime une personne identifiée par son prénom et son nom.
     *
     * @param firstName le prénom de la personne à supprimer (ex. : "John").
     * @param lastName le nom de la personne à supprimer (ex. : "Doe").
     * @return une réponse HTTP 204 (No Content) si la suppression est réussie, même si la personne n'existait pas.
     */
    @DeleteMapping()
    // /person?firstName=xxx&lastName=YYY
    public ResponseEntity<?> deletePerson(@RequestParam  String firstName, @RequestParam  String lastName) {
        log.info("Requête DELETE /person reçue pour {} {}", firstName, lastName);
        personService.removePerson(firstName, lastName);
        log.info("Personne supprimée avec succès : {} {}", firstName, lastName);
        return ResponseEntity.noContent().build();
    }
}
