package com.safetynet.AppSafetyNet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Représente une personne avec ses informations personnelles.
 * Utilisée dans le cadre des opérations de gestion de la sécurité.
 */
@Data
public class Person implements UniqueEntity {

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    private String city;

    @JsonProperty("zip")
    private String zip;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    public String getAddressComplete() {
        return (address + " " + zip + " " + city);
    }

}
