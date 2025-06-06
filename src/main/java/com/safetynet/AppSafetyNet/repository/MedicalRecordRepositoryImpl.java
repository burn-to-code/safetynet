package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

@Slf4j
@Service
public class MedicalRecordRepositoryImpl implements MedicalRecordRepository {

    private final DataStorage dataStorageService;


    public MedicalRecordRepositoryImpl(DataStorage dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    @Override
    public Optional<MedicalRecord> findByFirstNameAndLastName(String firstName, String lastName) {
        return dataStorageService.getMedicalRecords()
                .stream()
                .filter(p -> p.getFirstName().equals(firstName) && p.getLastName().equals(lastName))
                .findFirst();
    }

    @Override
    public void saveOrUpdateMedicalRecord(MedicalRecord medicalRecord) {
        dataStorageService.getMedicalRecords().removeIf(m -> m.getId().equals(medicalRecord.getId()));
        dataStorageService.getMedicalRecords().add(medicalRecord);
        dataStorageService.saveData();
    }

    @Override
    public void deleteMedicalRecord(MedicalRecord medicalRecord) {
        dataStorageService.getMedicalRecords().remove(medicalRecord);
        dataStorageService.saveData();
    }

    @Override
    public MedicalRecord getMedicalRecordByPerson(String firstName, String lastName) {
        Assert.notNull(firstName, "First name must not be null");
        Assert.notNull(lastName, "Last name must not be null");

        return findByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(() -> new IllegalStateException("Medical record for this person does not exist"));
    }
}
