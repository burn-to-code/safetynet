package com.safetynet.AppSafetyNet.model;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * DTO contenant les informations personnelles basiques d'une personne.
 * <p>
 * Utilisé pour transmettre des données essentielles sur une personne à l'utilisateur de l'API.
 */
@Deprecated
@Data
@AllArgsConstructor
public class PersonInfoDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
}
