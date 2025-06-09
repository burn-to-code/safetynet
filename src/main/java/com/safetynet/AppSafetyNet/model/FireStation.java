package com.safetynet.AppSafetyNet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Représente une affectation d'adresse à une caserne de pompiers.
 * Utilisée pour déterminer les zones couvertes par chaque station.
 */
@Data
public class FireStation {

    @JsonProperty("address")
    private String address;

    @JsonProperty("station")
    private String station;
}
