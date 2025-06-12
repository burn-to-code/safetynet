package com.safetynet.AppSafetyNet.model.dto;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;
import java.util.List;

public record FloodResponseDTO(
        String Address,
        List<PersonInfoDTO> personInfo

) {
    public FloodResponseDTO(String Address, List<PersonInfoDTO> personInfo) {
        this.Address = Address;
        this.personInfo = personInfo;
    }

    public record PersonInfoDTO(
            List<String> infoNameAndMedicationsAndAllergies,
            String numberPhone,
            int age
    ){
        public PersonInfoDTO(Person person, MedicalRecord mr) {
            this(
                    List.of(
                            person.getFirstName(),
                            person.getLastName(),
                            "Médicaments: " + String.join(", ", mr.getMedications()),
                            "Allergies: " + String.join(", ", mr.getAllergies())
                    ),
            person.getPhone(),
            mr.getAge()
            );
        }
    }

}
