package com.safetynet.AppSafetyNet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Data
public class MedicalRecord implements UniqueEntity {

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("birthdate")
    private String birthDate;

    @JsonProperty("medications")
    private List<String> medications;

    @JsonProperty("allergies")
    private List<String> allergies;

    public boolean isMajor() {
        if (birthDate == null || birthDate.isEmpty()) {
            log.warn("Birthdate is null or empty");
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate date = LocalDate.parse(birthDate, formatter);
            return Period.between(date, LocalDate.now()).getYears() > 18;
        } catch (DateTimeParseException e) {
            log.error("Erreur lors de la transformation de la date de naissance : {}", e.getMessage());
            return false;
        }
    }
}
