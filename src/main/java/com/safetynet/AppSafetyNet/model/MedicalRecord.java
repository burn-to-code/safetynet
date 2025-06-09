package com.safetynet.AppSafetyNet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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
        return parseBirthDate()
                .map(date -> Period.between(date, LocalDate.now()).getYears() > 18)
                .orElse(false);
    }

    public int getAge() {
        return parseBirthDate()
                .map(date -> Period.between(date, LocalDate.now()).getYears())
                .orElse(0);
    }

    private Optional<LocalDate> parseBirthDate() {
        if (birthDate == null || birthDate.isEmpty()) {
            log.warn("Birthdate is null or empty");
            return Optional.empty();
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            return Optional.of(LocalDate.parse(birthDate, formatter));
        } catch (DateTimeParseException e) {
            log.error("Erreur lors de la transformation de la date de naissance : {}", e.getMessage());
            return Optional.empty();
        }
    }
}
