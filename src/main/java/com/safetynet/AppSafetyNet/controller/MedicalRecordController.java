package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medicalrecord")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;


    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping
    public ResponseEntity<?> addMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        medicalRecordService.saveMedicalRecord(medicalRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecord);
    }

    @PutMapping()
    public ResponseEntity<?> updateMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        medicalRecordService.updateMedicalRecord( medicalRecord);
        return ResponseEntity.ok(medicalRecord);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteMedicalRecord(@RequestParam String firstName,  @RequestParam  String lastName) {
        medicalRecordService.deleteMedicalRecord(firstName, lastName);
        return ResponseEntity.noContent().build();
    }
}
