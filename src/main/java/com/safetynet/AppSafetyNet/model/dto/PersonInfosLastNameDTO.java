package com.safetynet.AppSafetyNet.model.dto;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;

import java.util.List;

public record PersonInfosLastNameDTO(
        String firstName,
        String lastName,
        String address,
        int age,
        String mail,
        List<String> medications,
        List<String> allergies
) {
    public PersonInfosLastNameDTO(Person person, MedicalRecord mr) {
        this(
                person.getFirstName(),
                person.getLastName(),
                person.getAddressComplete(),
                mr.getAge(),
                person.getEmail(),
                mr.getMedications(),
                mr.getAllergies()
        );
    }
}
