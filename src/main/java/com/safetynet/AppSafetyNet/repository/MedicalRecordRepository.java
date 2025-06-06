package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.MedicalRecord;

import java.util.Optional;

public interface MedicalRecordRepository {
    Optional<MedicalRecord> findByFirstNameAndLastName(String firstName, String lastName);
    void saveOrUpdateMedicalRecord(MedicalRecord medicalRecord);
    void deleteMedicalRecord(MedicalRecord medicalRecord);
    MedicalRecord getMedicalRecordByPerson(String firstName, String lastName);
}
