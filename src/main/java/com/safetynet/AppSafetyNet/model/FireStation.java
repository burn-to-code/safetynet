package com.safetynet.AppSafetyNet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FireStation {

    @JsonProperty("address")
    private String address;

    @JsonProperty("station")
    private int station;
}
