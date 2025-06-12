package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.Person;

import java.util.List;
import java.util.Optional;

/**
 * Interface définissant les opérations de persistance pour l'entité Person.
 * Permet l'accès, la sauvegarde et la suppression des données liées aux personnes.
 */
public interface PersonRepository {

    /**
     * Récupère toutes les personnes.
     * @return liste complète des personnes.
     */
    List<Person> getAll();


    /**
     * Recherche une personne par prénom et nom.
     * @param firstName prénom de la personne.
     * @param lastName nom de famille de la personne.
     * @return un Optional contenant la personne si elle est trouvée.
     */
    Optional<Person> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Enregistre ou met à jour une personne.
     * @param person l'objet Person à enregistrer.
     */
    void save(Person person);

    /**
     * Supprime une personne du système.
     * @param person l'objet Person à supprimer.
     */
    void delete(Person person);

    /**
     * Recherche les personnes habitant à une ou plusieurs adresses.
     * @param address liste d'adresses.
     * @return liste des personnes correspondant aux adresses.
     */
    List<Person> findByAddress(List<String> address);

    /**
     * Recherche les personnes habitant à une adresse unique.
     * @param address une seule adresse.
     * @return liste des personnes à cette adresse.
     */
    List<Person> findByAddress(String address);

    List<Person> findAllByLastName(String lastName);

}
