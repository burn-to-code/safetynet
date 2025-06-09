package com.safetynet.AppSafetyNet.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
/**
 * DTO représentant les informations d'un enfant vivant à une adresse donnée
 * ainsi que la liste des autres personnes vivant sous le même toit.
 */
@Data
@AllArgsConstructor
public class ChildAlertDTO {
    private String firstName;
    private String lastName;
    private int age;
    private List<String> personsInSameHouse;
}
