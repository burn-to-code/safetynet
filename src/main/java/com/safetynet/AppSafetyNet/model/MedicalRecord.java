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

/**
 * Représente le dossier médical d'une personne.
 * Contient les informations personnelles, la date de naissance, les médicaments et allergies.
 * <p>
 * Fournit également des méthodes utilitaires pour connaître l'âge ou la majorité de la personne.
 * </p>
 */
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

    /**
     * Indique si la personne est majeure (plus de 18 ans).
     *
     * @return {@code true} si l'âge est supérieur à 18 ans, sinon {@code false}.
     */
    public boolean isMajor() {
        return parseBirthDate()
                .map(date -> Period.between(date, LocalDate.now()).getYears() > 18)
                .orElse(false);
    }

    /**
     * Calcule l'âge de la personne à partir de la date de naissance.
     *
     * @return l'âge en années, ou 0 si la date est invalide.
     */
    public int getAge() {
        return parseBirthDate()
                .map(date -> Period.between(date, LocalDate.now()).getYears())
                .orElse(0);
    }

    /**
     * Tente de parser la date de naissance en objet {@link LocalDate}.
     *
     * @return un {@link Optional} contenant la date parsée, ou vide en cas d'erreur.
     */
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
