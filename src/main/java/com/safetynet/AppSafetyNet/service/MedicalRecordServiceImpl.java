package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Implémentation du service de gestion des dossiers médicaux.
 * Effectue les vérifications et délègue la persistance au repository.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveMedicalRecord(MedicalRecord medicalRecord) {
        Assert.notNull(medicalRecord, "Medical record must not be null");

        if (medicalRecordRepository.findByFirstNameAndLastName(medicalRecord.getFirstName(), medicalRecord.getLastName()).isPresent()) {
            throw new IllegalStateException("Medical record for this person already exists");
        }

        medicalRecordRepository.saveOrUpdateMedicalRecord(medicalRecord);
        log.info("Medical record saved successfully");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Met à jour les données d’un dossier médical existant
     * après vérification que celui-ci existe bien.
     * </p>
     */
    @Override
    public void updateMedicalRecord(MedicalRecord medicalRecord) {
        Assert.notNull(medicalRecord, "Medical record must not be null");

        MedicalRecord mr = medicalRecordRepository.findByFirstNameAndLastName(medicalRecord.getFirstName(), medicalRecord.getLastName())
               .orElseThrow(() -> new IllegalStateException("Medical record for this person does not exist"));

        mr.setBirthDate(medicalRecord.getBirthDate());
        mr.setMedications(medicalRecord.getMedications());
        mr.setAllergies(medicalRecord.getAllergies());

        medicalRecordRepository.saveOrUpdateMedicalRecord(mr);
        log.info("Medical record Updated successfully");
    }


    /**
     * {@inheritDoc}
     * <p>
     * Supprime un dossier médical existant après vérification.
     * </p>
     */
    @Override
    public void deleteMedicalRecord(String firstName, String lastName) {
        Assert.notNull(firstName, "First name must not be null");
        Assert.notNull(lastName, "Last name must not be null");

        MedicalRecord mr = medicalRecordRepository.findByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(() -> new IllegalStateException("Medical record for this person does not exist"));

        medicalRecordRepository.deleteMedicalRecord(mr);
        log.info("Medical record deleted successfully");
    }
}
