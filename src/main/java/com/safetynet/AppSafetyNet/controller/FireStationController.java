package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.dto.PersonCoveredDTO;
import com.safetynet.AppSafetyNet.service.FireStationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour gérer les opérations CRUD sur les casernes de pompiers
 * et récupérer les personnes couvertes par une station donnée.
 */
@Slf4j
@RestController
@RequestMapping("/firestation")
public class FireStationController {

    private final FireStationService fireStationService;

    /**
     * Constructeur avec injection du service FireStationService.
     *
     * @param fireStationService service métier pour gérer les casernes de pompiers.
     */
    public FireStationController(FireStationService fireStationService) {
        this.fireStationService = fireStationService;
    }

    /**
     * Ajoute une nouvelle caserne de pompiers.
     *
     * @param fs La caserne a ajouté (dans le corps de la requête).
     * @return La caserne ajoutée avec un code HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<?> addFireStation(@RequestBody FireStation fs) {
        log.info("Requête POST /firestation reçue avec payload : {}", fs);
        try {
            fireStationService.saveFireStation(fs);
            log.info("Caserne de pompiers créée avec succès : {}", fs);
            return ResponseEntity.status(HttpStatus.CREATED).body(fs);
        } catch (IllegalStateException e) {
            log.error("Erreur lors de la création de la caserne : {}", fs);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Met à jour une caserne de pompiers existante.
     *
     * @param fs La caserne mise à jour (dans le corps de la requête).
     * @return La caserne mise à jour avec un code HTTP 200 (OK).
     */
    @PutMapping()
    public ResponseEntity<?> updateFireStation(@RequestBody FireStation fs) {
        log.info("Requête PUT /firestation reçue avec payload : {}", fs);
        try {
            fireStationService.updateFireStation(fs);
            log.info("Caserne de pompiers mise à jour avec succès : {}", fs);
            return ResponseEntity.ok(fs);
        } catch (IllegalStateException e) {
            log.error("Erreur lors de la mise à jour de la caserne : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    /**
     * Supprime une caserne de pompiers par son adresse.
     *
     * @param address Adresse de la caserne à supprimer (paramètre de requête).
     * @return Un code HTTP 204 (No Content) indiquant que la suppression a réussi.
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteFireStation(@RequestParam String address) {
        log.info("Requête DELETE /firestation reçue pour l'adresse : {}", address);
        try {
            fireStationService.deleteFireStation(address);
            log.info("Caserne de pompiers supprimée avec succès pour l'adresse : {}", address);
            return ResponseEntity.noContent().build();
        }  catch (IllegalStateException e) {
            log.error("Erreur lors de la suppression de la caserne : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    /**
     * Récupère les personnes couvertes par une station donnée.
     *
     * @param stationNumber Numéro de la station (paramètre de requête).
     * @return DTO contenant la liste des personnes couvertes et le nombre d'adultes et enfants.
     */
    @GetMapping
    public ResponseEntity<?> getFireStation(@RequestParam String stationNumber) {
        log.info("Requête GET /firestation reçue pour stationNumber : {}", stationNumber);
        try {
            PersonCoveredDTO response = fireStationService.getPersonCoveredByNumberStation(stationNumber);
            log.info("Liste des personnes couvertes retournée avec succès pour la station : {}", stationNumber);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.error("Erreur lors de la récupération des personnes couvertes : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
