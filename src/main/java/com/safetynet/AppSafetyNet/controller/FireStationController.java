package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.PersonCoveredDTO;
import com.safetynet.AppSafetyNet.service.FireStationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour gérer les opérations CRUD sur les casernes de pompiers
 * et récupérer les personnes couvertes par une station donnée.
 */
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
        fireStationService.saveFireStation(fs);
        return ResponseEntity.status(HttpStatus.CREATED).body(fs);
    }

    /**
     * Met à jour une caserne de pompiers existante.
     *
     * @param fs La caserne mise à jour (dans le corps de la requête).
     * @return La caserne mise à jour avec un code HTTP 200 (OK).
     */
    @PutMapping()
    public ResponseEntity<?> updateFireStation(@RequestBody FireStation fs) {
        fireStationService.updateFireStation(fs);
        return ResponseEntity.ok(fs);
    }

    /**
     * Supprime une caserne de pompiers par son adresse.
     *
     * @param address Adresse de la caserne à supprimer (paramètre de requête).
     * @return Un code HTTP 204 (No Content) indiquant que la suppression a réussi.
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteFireStation(@RequestParam String address) {
        fireStationService.deleteFireStation(address);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les personnes couvertes par une station donnée.
     *
     * @param stationNumber Numéro de la station (paramètre de requête).
     * @return DTO contenant la liste des personnes couvertes et le nombre d'adultes et enfants.
     */
    @GetMapping
    public ResponseEntity<?> getFireStation(@RequestParam String stationNumber) {
        PersonCoveredDTO response = fireStationService.getPersonCoveredByNumberStation(stationNumber);
        return ResponseEntity.ok(response);
    }
}
