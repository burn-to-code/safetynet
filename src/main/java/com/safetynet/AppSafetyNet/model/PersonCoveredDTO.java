package com.safetynet.AppSafetyNet.model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PersonCoveredDTO {
    private List<PersonInfoDTO> persons;
    private int adults;
    private int children;
}
