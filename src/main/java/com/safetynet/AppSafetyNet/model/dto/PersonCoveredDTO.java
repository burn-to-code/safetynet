package com.safetynet.AppSafetyNet.model.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;

import java.util.List;

/**
 * DTO regroupant des informations sur un groupe de personnes couvertes.
 * <p>
 * Contient la liste des personnes ainsi que le nombre d'adultes et d'enfants dans ce groupe.
 * <p>
 * Utilisé pour transmettre un résumé des personnes et leur classification par âge à l'utilisateur de l'API.
 */

public record PersonCoveredDTO (
            List<PersonInfoDTO> persons,
            long adults,
            long children
        ){


    public PersonCoveredDTO(List<Person> personList , List<MedicalRecord>  medicalRecords) {
       this(personList.stream().map(PersonInfoDTO::new).toList(),
            medicalRecords.stream().filter(MedicalRecord::isMajor).count(),
               medicalRecords.size() -  medicalRecords.stream().filter(MedicalRecord::isMajor).count());
    }


    public PersonCoveredDTO {
        if (persons.size() != adults + children){
            throw new IllegalArgumentException("Number of persons and adults do not match");
        }
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PersonInfoDTO
            (String firstName,
             String lastName,
             String address,
             String phone
            ){

        public PersonInfoDTO(Person person) {
            this(person.getFirstName(), person.getLastName(), person.getAddressComplete(), person.getPhone());
        }
    }

}
