package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.service.MedicalRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour gérer les opérations CRUD sur les dossiers médicaux.
 */
@Slf4j
@RestController
@RequestMapping("/medicalrecord")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * Constructeur avec injection du service MedicalRecordService.
     *
     * @param medicalRecordService service métier pour gérer les dossiers médicaux.
     */
    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }


    /**
     * Ajoute un nouveau dossier médical pour une personne.
     *
     * @param medicalRecord Le dossier médical à ajouter (fourni dans le corps de la requête).
     * @return Une réponse HTTP 201 (Created) contenant le dossier ajouté.
     * @throws com.safetynet.AppSafetyNet.exception.ConflictException si un dossier médical existe déjà pour cette personne.
     * @throws IllegalArgumentException si l'objet fourni est null.
     */

    @PostMapping
    public ResponseEntity<?> addMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        log.info("Requête POST /medicalrecord reçue avec payload : {}", medicalRecord);
        medicalRecordService.saveMedicalRecord(medicalRecord);
        log.info("Dossier médical créé avec succès : {}", medicalRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecord);
    }


    /**
     * Met à jour un dossier médical existant identifié par le prénom et le nom.
     *
     * @param medicalRecord Le dossier médical avec les nouvelles données (dans le corps de la requête).
     * @return Une réponse HTTP 200 (OK) contenant le dossier mis à jour.
     * @throws com.safetynet.AppSafetyNet.exception.NotFoundException si aucun dossier médical n'existe pour cette personne.
     * @throws IllegalArgumentException si l'objet est null.
     */
    @PutMapping()
    public ResponseEntity<?> updateMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        log.info("Requête PUT /medicalrecord reçue avec payload : {}", medicalRecord);
        medicalRecordService.updateMedicalRecord( medicalRecord);
        log.info("Dossier médical mis à jour avec succès : {}", medicalRecord);
        return ResponseEntity.ok(medicalRecord);
    }


    /**
     * Supprime un dossier médical à partir du prénom et du nom d’une personne.
     *
     * @param firstName Prénom de la personne (paramètre de requête).
     * @param lastName  Nom de la personne (paramètre de requête).
     * @return Une réponse HTTP 204 (No Content) indiquant que la suppression a réussi.
     * @throws IllegalArgumentException si le prénom ou le nom est null.
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteMedicalRecord(@RequestParam String firstName,  @RequestParam  String lastName) {
        log.info("Requête DELETE /medicalrecord reçue pour {} {}", firstName, lastName);
        medicalRecordService.deleteMedicalRecord(firstName, lastName);
        log.info("Dossier médical supprimé avec succès pour {} {}", firstName, lastName);
        return ResponseEntity.noContent().build();
    }
}
