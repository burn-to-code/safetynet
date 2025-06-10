package com.safetynet.AppSafetyNet.model.dto;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.model.UniqueEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO représentant les informations d'un enfant vivant à une adresse donnée
 * ainsi que la liste des autres personnes vivant sous le même toit.
 */
public record ChildAlertDTO(
        String firstName,
        String lastName,
        int age,
        List<String> personsInSameHouse
){
    public ChildAlertDTO(Person child, List<Person> personsAtAddress, MedicalRecord mr) {
        this(child.getFirstName(),
                child.getLastName(),
                mr != null ? mr.getAge() : 0,
                personsAtAddress.stream()
                        .filter(p -> p.getLastName().equals(child.getLastName()) && !p.getFirstName().equals(child.getFirstName()))
                        .map(UniqueEntity::getId)
                        .collect(Collectors.toList())
        );
    }
}
