package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour gérer les opérations CRUD sur les dossiers médicaux.
 */
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
     * Ajoute un nouveau dossier médical.
     *
     * @param medicalRecord Le dossier médical à ajouter (dans le corps de la requête).
     * @return Le dossier médical créé avec un code HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<?> addMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        try {
            medicalRecordService.saveMedicalRecord(medicalRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecord);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }


    /**
     * Met à jour un dossier médical existant.
     *
     * @param medicalRecord Le dossier médical avec les données mises à jour (dans le corps de la requête).
     * @return Le dossier médical mis à jour avec un code HTTP 200 (OK).
     */
    @PutMapping()
    public ResponseEntity<?> updateMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        try {
            medicalRecordService.updateMedicalRecord( medicalRecord);
            return ResponseEntity.ok(medicalRecord);
        }  catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

    }


    /**
     * Supprime un dossier médical par prénom et nom.
     *
     * @param firstName Prénom de la personne dont on supprime le dossier médical (paramètre de requête).
     * @param lastName  Nom de la personne dont on supprime le dossier médical (paramètre de requête).
     * @return Un code HTTP 204 (No Content) indiquant que la suppression a réussi.
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteMedicalRecord(@RequestParam String firstName,  @RequestParam  String lastName) {
        try {
            medicalRecordService.deleteMedicalRecord(firstName, lastName);
            return ResponseEntity.noContent().build();
        }  catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }
}
