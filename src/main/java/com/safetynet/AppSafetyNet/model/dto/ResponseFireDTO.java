package com.safetynet.AppSafetyNet.model.dto;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;

import java.util.List;

public record ResponseFireDTO(
        List<PersonsFireDTO> persons,
        String stationNumber
){
    public ResponseFireDTO(List<Person> persons, List<MedicalRecord> medicalRecords, FireStation fireStation){
        this(persons.stream()
                .map(p -> {
                    MedicalRecord medicalRecord = medicalRecords.stream()
                            .filter(mr -> mr.getId().equals(p.getId()))
                            .findFirst()
                            .orElse(null);
                    assert medicalRecord != null;
                    return new PersonsFireDTO(p, medicalRecord);
                })
                .toList(),
            fireStation.getStation()
        );
    }

    public record PersonsFireDTO(
            PersonCoveredDTO.PersonInfoDTO persons,
            List<String> medications,
            List<String> allergies
    ) {
        public PersonsFireDTO(Person person, MedicalRecord medicalRecord) {
            this(new PersonCoveredDTO.PersonInfoDTO(person),
                    medicalRecord.getMedications(),
                    medicalRecord.getAllergies());
        }
    }

}
