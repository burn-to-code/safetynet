package com.safetynet.AppSafetyNet.repository.Impl;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Implémentation concrète du MedicalRecordRepository.
 * Utilise DataStorage comme source de données.
 *
 * @see DataStorage
 */
@Slf4j
@Service
public class MedicalRecordRepositoryImpl implements MedicalRecordRepository {

    private final DataStorage dataStorageService;

    public MedicalRecordRepositoryImpl(DataStorage dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    /**
     * Recherche un dossier médical selon le prénom et nom.
     */
    @Override
    public Optional<MedicalRecord> findByFirstNameAndLastName(String firstName, String lastName) {
        Assert.notNull(firstName, "firstName must not be null");
        Assert.notNull(lastName, "lastName must not be null");
        return dataStorageService.getMedicalRecords()
                .stream()
                .filter(p -> p.getFirstName().equals(firstName) && p.getLastName().equals(lastName))
                .findFirst();
    }

    /**
     * Sauvegarde ou met à jour un dossier médical.
     * Supprime tout doublon basé sur l'id.
     */
    @Override
    public void saveOrUpdateMedicalRecord(MedicalRecord medicalRecord) {
        Assert.notNull(medicalRecord, "Medical Record must not be null");
        dataStorageService.getMedicalRecords().removeIf(m -> m.getId().equals(medicalRecord.getId()));
        dataStorageService.getMedicalRecords().add(medicalRecord);
        dataStorageService.saveData();
        log.info("Medical Record saved successfully: {}", medicalRecord);
    }

    /**
     * Supprime un dossier médical existant.
     */
    @Override
    public void deleteMedicalRecord(MedicalRecord medicalRecord) {
        Assert.notNull(medicalRecord, "Medical Record must not be null");
        dataStorageService.getMedicalRecords().remove(medicalRecord);
        dataStorageService.saveData();
        log.info("Medical Record deleted successfully : {}", medicalRecord);
    }

    /**
     * Récupère un dossier médical de manière obligatoire.
     * Lève une exception s'il n'existe pas.
     */
    @Override
    public MedicalRecord getMedicalRecordByPerson(String firstName, String lastName) {
        Assert.notNull(firstName, "First name must not be null");
        Assert.notNull(lastName, "Last name must not be null");

        return findByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(() -> new IllegalStateException("Medical record for this person does not exist"));
    }
}
