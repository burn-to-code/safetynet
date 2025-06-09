package com.safetynet.AppSafetyNet.model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO regroupant des informations sur un groupe de personnes couvertes.
 * <p>
 * Contient la liste des personnes ainsi que le nombre d'adultes et d'enfants dans ce groupe.
 * <p>
 * Utilisé pour transmettre un résumé des personnes et leur classification par âge à l'utilisateur de l'API.
 */
@Data
@AllArgsConstructor
public class PersonCoveredDTO {
    private List<PersonInfoDTO> persons;
    private int adults;
    private int children;
}
