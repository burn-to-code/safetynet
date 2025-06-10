package com.safetynet.AppSafetyNet.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

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

    @JsonFormat(pattern = "MM/dd/yyyy")
    @JsonProperty("birthdate")
    private LocalDate birthDate;

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
        return  getAge() > 18 ;
    }

    public boolean isMinor() {
        return !isMajor();
    }

    /**
     * Calcule l'âge de la personne à partir de la date de naissance.
     *
     * @return l'âge en années, ou 0 si la date est invalide.
     */
    public int getAge() {
        Assert.notNull(birthDate, "Birthdate must not be null");
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Tente de parser la date de naissance en objet {@link LocalDate}.
     *
     * @return un {@link Optional} contenant la date parsée, ou vide en cas d'erreur.
     */
//    private Optional<LocalDate> parseBirthDate() {
//        if (birthDate == null || birthDate.isEmpty()) {
//            log.warn("Birthdate is null or empty");
//            return Optional.empty();
//        }
//
//        try {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
//            return Optional.of(LocalDate.parse(birthDate, formatter));
//        } catch (DateTimeParseException e) {
//            log.error("Erreur lors de la transformation de la date de naissance : {}", e.getMessage());
//            return Optional.empty();
//        }
//    }
}
