package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.MedicalRecord;

import java.util.Optional;

/**
 * Interface de persistance pour les dossiers médicaux (MedicalRecord).
 */
public interface MedicalRecordRepository {

    /**
     * Recherche un dossier médical à partir du prénom et nom d'une personne.
     * @param firstName Prénom
     * @param lastName Nom
     * @return Un Optional contenant le dossier médical si trouvé.
     */
    Optional<MedicalRecord> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Enregistre un nouveau dossier médical ou met à jour un dossier existant.
     * @param medicalRecord L'objet MedicalRecord à sauvegarder.
     */
    void saveOrUpdateMedicalRecord(MedicalRecord medicalRecord);

    /**
     * Supprime un dossier médical existant.
     * @param medicalRecord L'objet MedicalRecord à supprimer.
     */
    void deleteMedicalRecord(MedicalRecord medicalRecord);


    /**
     * Récupère un dossier médical de manière obligatoire (erreur si non trouvée.).
     * @param firstName Prénom
     * @param lastName Nom
     * @return Le dossier médical correspondant.
     */
    MedicalRecord getMedicalRecordByPerson(String firstName, String lastName);
}
