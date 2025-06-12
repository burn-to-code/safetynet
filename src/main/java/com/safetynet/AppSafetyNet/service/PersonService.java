package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.dto.ChildAlertDTO;
import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.model.dto.FloodResponseDTO;
import com.safetynet.AppSafetyNet.model.dto.PersonInfosLastNameDTO;
import com.safetynet.AppSafetyNet.model.dto.ResponseFireDTO;

import java.util.List;

/**
 * Service d'accès et gestion des données relatives aux personnes.
 * Définit les opérations principales de création, suppression, mise à jour,
 * ainsi que des requêtes spécifiques liées aux enfants et aux numéros de téléphone
 * en fonction d’adresses ou de stations de pompiers.
 */
public interface PersonService {

    /**
     * Ajoute une nouvelle personne en base.
     * @param person la personne à ajouter, ne doit pas être nulle
     * @throws IllegalArgumentException si la personne existe déjà
     */
    void addPerson(Person person);

    /**
     * Supprime une personne identifiée par son prénom et nom.
     * @param firstName prénom de la personne, ne doit pas être nul
     * @param lastName nom de la personne, ne doit pas être nul
     */
    void removePerson(String firstName, String lastName);

    /**
     * Met à jour les informations d'une personne existante.
     * @param person objet Person avec les nouvelles informations, ne doit pas être nul
     * @throws IllegalArgumentException si la personne n'existe pas
     */
    void updatePerson(Person person);

    /**
     * Récupère la liste des enfants (moins de 18 ans) vivant à une adresse donnée,
     * ainsi que les autres personnes du foyer.
     * @param address adresse pour laquelle rechercher les enfants, ne doit pas être nulle
     * @return liste d'objets ChildAlertDTO contenant les informations des enfants et des personnes du foyer
     */
    List<ChildAlertDTO> getChildrenByAddress(String address);

    /**
     * Récupère la liste des numéros de téléphone des personnes couvertes par une caserne de pompiers.
     * @param fireStationNumber numéro de la caserne de pompiers
     * @return liste des numéros de téléphone distincts
     */
    List<String> getPhoneNumbersByFireStation(String fireStationNumber);

    ResponseFireDTO getPersonnesAndStationNumberByAddress(String address);

    List<FloodResponseDTO> getPersonnesAndAddressByNumberFireStation(List<String> fireStationNumber);

    List<PersonInfosLastNameDTO>  getPersonsByLastName(String lastName);

    List<String> getMailByCity(String city);

}
