package com.safetynet.AppSafetyNet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface définissant une entité unique par son prénom et son nom.
 * Permet de générer un identifiant basé sur ces deux champs.
 */
public interface UniqueEntity {
    /**
     * Retourne un identifiant unique basé sur le prénom et le nom.
     *
     * @return identifiant concaténé : firstName + lastName (espacé)
     */
    @JsonIgnore
    default String getId(){
        return getFirstName() + " " + getLastName();
    }

    String getFirstName();

    String getLastName();
}
