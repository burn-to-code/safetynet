package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.FireStation;

import java.util.List;
import java.util.Optional;

/**
 * Interface définissant les opérations de persistance pour l'entité FireStation.
 * Gère les correspondances entre adresses et numéros de caserne.
 */
public interface FireStationRepository {

    /**
     * Récupère toutes les casernes de pompiers enregistrées.
     * @return liste des objets FireStation.
     */
    List<FireStation> getAll();

    /**
     * Recherche une caserne de pompiers par son adresse.
     * @param address adresse recherchée.
     * @return un Optional contenant la caserne si elle existe.
     */
    Optional<FireStation> findByAddress(String address);


    /**
     * Sauvegarde ou met à jour une association adresse/caserne.
     * @param fs l'objet FireStation à enregistrer.
     */
    void saveFireStation(FireStation fs);

    /**
     * Supprime une association adresse/caserne.
     * @param fs l'objet FireStation à supprimer.
     */
    void deleteFireStation(FireStation fs);

    /**
     * Retourne toutes les adresses associées à un numéro de caserne donné.
     * @param number numéro de la caserne.
     * @return liste d'adresses.
     */
    List<String> findAddressByNumberStation(String number);
}
