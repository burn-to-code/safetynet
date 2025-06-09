package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire servant de conteneur pour désérialiser et sérialiser
 * les données structurées du fichier JSON source.
 * <p>
 * Cette classe est utilisée par Jackson pour mapper l'ensemble des données
 * dans une seule structure centrale en mémoire.
 * </p>
 *
 * @see JsonDataStorageImpl
 */
@Data
public class DataWrapper {
    /**
     * Liste des personnes stockées dans les données.
     */
    private List<Person> persons = new ArrayList<>();


    /**
     * Liste des casernes de pompiers associées à des adresses.
     */
    private List<FireStation>  firestations  = new ArrayList<>();


    /**
     * Liste des dossiers médicaux correspondant aux personnes.
     */
    private List<MedicalRecord>  medicalrecords  = new ArrayList<>();
}
