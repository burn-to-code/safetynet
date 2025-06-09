package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
/**
 * Service pour la gestion des dossiers médicaux.
 * Définit les opérations de création, mise à jour et suppression
 * des fiches médicales associées aux personnes.
 */
public interface MedicalRecordService {

    /**
     * Enregistre un nouveau dossier médical.
     * @param medicalRecord dossier médical à enregistrer, ne doit pas être nul
     * @throws IllegalStateException si un dossier existe déjà pour la personne
     */
    void saveMedicalRecord(MedicalRecord medicalRecord);

    /**
     * Met à jour un dossier médical existant.
     * @param medicalRecord dossier médical avec les nouvelles données, ne doit pas être nul
     * @throws IllegalStateException si le dossier médical n'existe pas
     */
    void updateMedicalRecord(MedicalRecord medicalRecord);

    /**
     * Supprime un dossier médical identifié par prénom et nom.
     * @param firstName prénom de la personne, ne doit pas être nul
     * @param lastName nom de la personne, ne doit pas être nul
     * @throws IllegalStateException si le dossier médical n'existe pas
     */
    void deleteMedicalRecord(String firstName, String lastName);

}
