package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.MedicalRecord;

public interface MedicalRecordService {

    void saveMedicalRecord(MedicalRecord medicalRecord);

    void updateMedicalRecord(MedicalRecord medicalRecord);

    void deleteMedicalRecord(String firstName, String lastName);

}
