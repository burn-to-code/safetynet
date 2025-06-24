package com.safetynet.AppSafetyNet.service.Impl;

import com.safetynet.AppSafetyNet.exception.ConflictException;
import com.safetynet.AppSafetyNet.exception.NotFoundException;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.service.MedicalRecordService;
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
        log.debug("Tentative de sauvegarde d'un dossier médical: {}", medicalRecord);
        Assert.notNull(medicalRecord, "Medical record must not be null");

        if (medicalRecordRepository.findByFirstNameAndLastName(medicalRecord.getFirstName(), medicalRecord.getLastName()).isPresent()) {
            log.info("Dossier médical déjà existant pour {}", medicalRecord.getId());
            throw new ConflictException("Medical record for this person already exists");
        }

        medicalRecordRepository.saveOrUpdateMedicalRecord(medicalRecord);
        log.info("Dossier médical sauvegardé avec succès pour {}", medicalRecord.getId());
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
        log.debug("Tentative de mise à jour du dossier médical: {}", medicalRecord);
        Assert.notNull(medicalRecord, "Medical record must not be null");

        MedicalRecord mr = medicalRecordRepository.findByFirstNameAndLastName(medicalRecord.getFirstName(), medicalRecord.getLastName())
               .orElseThrow(() -> {
                   log.info("Aucun dossier médical trouvé pour {} ", medicalRecord.getId());
                   return new NotFoundException("Medical record for this person does not exist");
               });

        mr.setBirthDate(medicalRecord.getBirthDate());
        mr.setMedications(medicalRecord.getMedications());
        mr.setAllergies(medicalRecord.getAllergies());

        medicalRecordRepository.saveOrUpdateMedicalRecord(mr);
        log.info("Dossier médical mis à jour avec succès pour {}", mr.getId());
    }


    /**
     * {@inheritDoc}
     * <p>
     * Supprime un dossier médical existant après vérification.
     * </p>
     */
    @Override
    public void deleteMedicalRecord(String firstName, String lastName) {
        log.debug("Tentative de suppression du dossier médical pour {} {}", firstName, lastName);
        Assert.notNull(firstName, "First name must not be null");
        Assert.notNull(lastName, "Last name must not be null");

        log.debug("Recherche du dossier médical: {}", firstName + " " + lastName);
        medicalRecordRepository.findByFirstNameAndLastName(firstName, lastName)
                .ifPresent(medicalRecordRepository::deleteMedicalRecord);
    }
}
