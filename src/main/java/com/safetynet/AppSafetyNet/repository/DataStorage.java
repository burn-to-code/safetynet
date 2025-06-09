package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;

import java.io.IOException;
import java.util.List;

/**
 * Interface définissant les opérations de persistance sur les données de l'application.
 * Fournit un contrat pour initialiser, charger, sauvegarder et accéder aux données depuis un fichier JSON.
 */
public interface DataStorage {

    /**
     * Copie le fichier JSON embarqué dans les ressources vers un fichier exploitable localement.
     * @throws IOException en cas d'erreur d'accès ou de copie de fichier.
     */
    void initializeDataFile()  throws IOException;

    /**
     * Charge les données depuis le fichier JSON local dans la mémoire (via un wrapper).
     * @throws IOException en cas d'erreur de lecture.
     */
    void loadData()  throws IOException;

    /**
     * Sauvegarde les données actuelles en mémoire dans le fichier JSON local.
     */
    void saveData();

    /**
     * Retourne la liste des personnes connues dans le système.
     * @return Liste d'objets Person.
     */
    List<Person> getPersons();

    /**
     * Retourne la liste des casernes associées à une adresse.
     * @return Liste d'objets FireStation.
     */
    List<FireStation>  getFireStations();


    /**
     * Retourne la liste des dossiers médicaux.
     * @return Liste d'objets MedicalRecord.
     */
    List<MedicalRecord> getMedicalRecords();
}
