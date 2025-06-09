package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;


/**
 * Implémentation de l'interface PersonRepository.
 * Utilise le service DataStorage pour manipuler les données de type Person.
 *
 * @see DataStorage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonRepositoryImpl implements PersonRepository {

    final private DataStorage dataStorageService;

    /**
     * Retourne toutes les personnes présentes dans les données.
     */
    @Override
    public List<Person> getAll() {
        return dataStorageService.getPersons();
    }

    /**
     * Recherche d'une personne par prénom et nom.
     */
    @Override
    public Optional<Person> findByFirstNameAndLastName(String firstName, String lastName) {
        Assert.notNull(firstName,  "First name must not be null");
        Assert.notNull(lastName,  "Last name must not be null");
        return dataStorageService.getPersons()
                .stream()
                .filter(p -> p.getFirstName().equals(firstName))
                .filter(p -> p.getLastName().equals(lastName))
                .findFirst();
    }

    /**
     * Sauvegarde (ou mise à jour) d'une personne.
     * Si l'ID (nom+prénom) existe déjà, l'ancien objet est remplacé.
     */
    @Override
    public void save(Person person) {
        dataStorageService.getPersons().removeIf(p -> p.getId().equals(person.getId()));
        dataStorageService.getPersons().add(person);
        dataStorageService.saveData();
        log.info("Person saved/updated: {} {}", person.getFirstName(), person.getLastName());
    }

    /**
     * Supprime une personne.
     */
    @Override
    public void delete(Person person) {
        dataStorageService.getPersons().remove(person);
        dataStorageService.saveData();
        log.info("Person deleted/updated: {} {}", person.getFirstName(), person.getLastName());
    }

    /**
     * Recherche toutes les personnes vivant dans l'une des adresses données.
     */
    @Override
    public List<Person> findByAddress(List<String> address) {
        return dataStorageService.getPersons()
                .stream()
                .filter(p -> address.contains(p.getAddress()))
                .toList();
    }

    /**
     * Surcharge de findByAddress pour une seule adresse.
     */
    @Override
    public List<Person> findByAddress(String address) {
        return findByAddress(List.of(address));
    }
}
