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
     * @throws com.safetynet.AppSafetyNet.exception.ConflictException si une caserne existe déjà à cette adresse.
     * @throws IllegalArgumentException si l'objet est null.
     */
    @PostMapping
    public ResponseEntity<?> addFireStation(@RequestBody FireStation fs) {
        log.info("Requête POST /firestation reçue avec payload : {}", fs);
        fireStationService.saveFireStation(fs);
        log.info("Caserne de pompiers créée avec succès : {}", fs);
        return ResponseEntity.status(HttpStatus.CREATED).body(fs);
    }

    /**
     * Met à jour une caserne de pompiers existante à partir de son adresse.
     *
     * @param fs La caserne contenant les nouvelles données (dans le corps de la requête).
     * @return La caserne mise à jour avec un code HTTP 200 (OK).
     * @throws com.safetynet.AppSafetyNet.exception.NotFoundException si aucune caserne n'existe à cette adresse.
     * @throws IllegalArgumentException si l'objet est null.
     */
    @PutMapping()
    public ResponseEntity<?> updateFireStation(@RequestBody FireStation fs) {
        log.info("Requête PUT /firestation reçue avec payload : {}", fs);
        fireStationService.updateFireStation(fs);
        log.info("Caserne de pompiers mise à jour avec succès : {}", fs);
        return ResponseEntity.ok(fs);
    }

    /**
     * Supprime une caserne de pompiers à partir de son adresse.
     *
     * @param address L'adresse de la caserne à supprimer (passée en paramètre de requête).
     * @return Un code HTTP 204 (No Content) indiquant une suppression réussie.
     * @throws IllegalArgumentException si l'adresse est null.
     */

    @DeleteMapping()
    public ResponseEntity<?> deleteFireStation(@RequestParam String address) {
        log.info("Requête DELETE /firestation reçue pour l'adresse : {}", address);
        fireStationService.deleteFireStation(address);
        log.info("Caserne de pompiers supprimée avec succès pour l'adresse : {}", address);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les informations des personnes couvertes par une station de pompiers.
     * <p>
     * Cette méthode retourne un DTO contenant la liste des personnes desservies par
     * la station ainsi que le nombre d’adultes et d’enfants.
     * </p>
     *
     * @param stationNumber Numéro de la station (passé en paramètre de requête).
     * @return Une réponse HTTP 200 avec un {@link PersonCoveredDTO} contenant les données demandées.
     * @throws com.safetynet.AppSafetyNet.exception.NotFoundException si aucune adresse n'est associée à cette station.
     * @throws com.safetynet.AppSafetyNet.exception.ErrorSystemException si un dossier médical est introuvable.
     */
    @GetMapping
    public ResponseEntity<?> getFireStation(@RequestParam Integer stationNumber) {
        log.info("Requête GET /firestation reçue pour stationNumber : {}", stationNumber);
        PersonCoveredDTO response = fireStationService.getPersonCoveredByNumberStation(stationNumber);
        log.info("Liste des personnes couvertes retournée avec succès pour la station : {}", stationNumber);
        return ResponseEntity.ok(response);
    }
}
