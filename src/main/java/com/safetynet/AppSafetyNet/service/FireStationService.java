package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.dto.PersonCoveredDTO;

/**
 * Service pour la gestion des casernes de pompiers (FireStation).
 * Fournit les opérations de création, mise à jour, suppression
 * ainsi qu'une méthode pour récupérer les personnes couvertes par un numéro de caserne.
 */
public interface FireStationService {

    /**
     * Enregistre une nouvelle caserne de pompiers.
     * @param fireStation la caserne à enregistrer, ne doit pas être nulle
     * @throws IllegalStateException si une caserne existe déjà à cette adresse
     */
    void saveFireStation (FireStation fireStation);

    /**
     * Met à jour une caserne existante.
     * @param updatedFireStation caserne mise à jour, ne doit pas être nulle
     * @throws IllegalStateException si la caserne n'existe pas
     */
    void updateFireStation (FireStation updatedFireStation);

    /**
     * Supprime une caserne de pompiers identifiée par son adresse.
     * @param address adresse de la caserne à supprimer, ne doit pas être nulle
     * @throws IllegalStateException si la caserne n'existe pas
     */
    void deleteFireStation (String address);

    /**
     * Récupère la liste des personnes couvertes par une caserne identifiée par son numéro.
     * @param stationNumber numéro de la caserne
     * @return un DTO contenant les informations des personnes couvertes,
     *         ainsi que le nombre d'adultes et d'enfants
     */
    PersonCoveredDTO getPersonCoveredByNumberStation(Integer stationNumber);
}
