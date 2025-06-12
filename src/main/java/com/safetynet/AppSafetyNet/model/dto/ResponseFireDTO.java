package com.safetynet.AppSafetyNet.model.dto;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;

import java.util.List;

public record ResponseFireDTO(
        List<PersonsFireDTO> persons,
        String stationNumber
){
    /**
     * Constructeur permettant d'instancier nos différentes PersonFireDTO.
     * Puis, de rajouter le numéro de la station correspondante à l'adresse (logique réalisé
     * dans le service)
     * @param persons une liste de person
     * @param medicalRecords une liste de dossier medical
     * @param fireStation une Station de pompier (doit être la bonne station, la méthode ne vérifie pas)
     */
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

    /**
     * Une forme de réponse de personne pour la requête /fire
     * @param persons un DTO utilisé précédemment, mais ayant la forme attendue pour notre requête
     * @param medications la liste de medicament de la personne
     * @param allergies la liste d'allergie de la personne
     */
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
