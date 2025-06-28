package com.safetynet.AppSafetyNet.service.Impl;

import com.safetynet.AppSafetyNet.exception.ConflictException;
import com.safetynet.AppSafetyNet.exception.ErrorSystemException;
import com.safetynet.AppSafetyNet.exception.NotFoundException;
import com.safetynet.AppSafetyNet.model.*;
import com.safetynet.AppSafetyNet.model.dto.ChildAlertDTO;
import com.safetynet.AppSafetyNet.model.dto.FloodResponseDTO;
import com.safetynet.AppSafetyNet.model.dto.PersonInfosLastNameDTO;
import com.safetynet.AppSafetyNet.model.dto.ResponseFireDTO;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import com.safetynet.AppSafetyNet.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository repository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final FireStationRepository fireStationRepository;


    /**
     * Ajoute une nouvelle personne si elle n'existe pas déjà.
     *
     * @param person la personne à ajouter.
     * @throws IllegalArgumentException si la personne est nulle.
     * @throws ConflictException si une personne avec le même prénom et nom existe déjà.
     */
    @Override
    public void addPerson(Person person) {
        Assert.notNull(person, "Person must not be null");
        log.debug("Tentative d'ajout d'une personne: {}", person.getId());


        if (repository.findByFirstNameAndLastName(person.getFirstName(), person.getLastName())
                .isPresent()){
            log.error("La personne {} existe déjà", person.getId());
            throw new ConflictException("Person already exists");
        }
        repository.save(person);
        log.info("Personne ajoutée avec succès: {}", person.getId());
    }

    /**
     * Supprime une personne à partir de son prénom et de son nom.
     *
     * @param firstName le prénom de la personne à supprimer.
     * @param lastName le nom de la personne à supprimer.
     * @throws IllegalArgumentException si l'un des paramètres est nul.
     */
    @Override
    public void removePerson(String firstName, String lastName) {
        Assert.notNull(firstName, "First name must not be null");
        Assert.notNull(lastName, "Last name must not be null");
        log.debug("Tentative de suppression de la personne: {} {}", firstName, lastName);
        repository.findByFirstNameAndLastName(firstName, lastName)
                .ifPresent(repository::delete);
    }


    /**
     * Met à jour les informations d'une personne existante.
     * Ne met pas à jour le prénom et le nom.
     *
     * @param person l'objet contenant les nouvelles données.
     * @throws NotFoundException si la personne n'existe pas.
     * @throws IllegalArgumentException si la personne est nulle.
     */
    @Override
    public void updatePerson(Person person) {
        Assert.notNull(person, "Person must not be null");
        log.debug("Mise à jour des informations pour: {}", person.getId());

        Person personToUpdate = repository.findByFirstNameAndLastName(person.getFirstName(), person.getLastName())
                .orElseThrow(() -> {
                    log.error("Personne non trouvée pour la mise à jour: {} {}", person.getFirstName(), person.getLastName());
                    return new NotFoundException("Person not found : " + person.getId());
                });

        personToUpdate.setCity(person.getCity());
        personToUpdate.setZip(person.getZip());
        personToUpdate.setAddress(person.getAddress());
        personToUpdate.setEmail(person.getEmail());
        personToUpdate.setPhone(person.getPhone());

        repository.save(personToUpdate);
        log.info("Personne mise à jour avec succès: {}", person.getId());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Recherche les personnes vivant à l'adresse donnée,
     * filtre celles qui sont mineures selon leur fiche médicale,
     * puis construit une liste DTO contenant les enfants avec leur âge
     * et les autres membres du foyer.
     * </p>
     *
     * Retourne les enfants habitant à une adresse donnée, accompagnés des autres membres du foyer.
     * @param address l'adresse à rechercher.
     * @return une liste de {@link ChildAlertDTO} contenant les enfants et leur entourage.
     * @throws IllegalArgumentException si l'adresse est vide ou nulle.
     * @throws ErrorSystemException si un dossier médical est manquant.
     *
     */
    @Override
    public List<ChildAlertDTO> getChildrenByAddress(String address) {
        validateString(address, "address");
        log.debug("Recherche des enfants à l'adresse: {}", address);

        List<Person> personsAtAddress = repository.findByAddress(address);

        if (personsAtAddress.isEmpty()){
            log.info("Aucune personne trouvée à l'adresse: {}", address);
            return Collections.emptyList();
        }

        List<Person> children = personsAtAddress.stream()
                .filter(person -> {
                    MedicalRecord mr = medicalRecordRepository.findByFirstNameAndLastName(person.getFirstName(), person.getLastName())
                            .orElseThrow(()  -> new ErrorSystemException("Medical record not found : " + person.getFirstName() + " " + person.getLastName()));
                    return mr != null && !mr.isMajor();
                })
                .toList();

        if(children.isEmpty()){
            log.info("Aucun enfant trouvé à l'adresse: {}", address);
            return Collections.emptyList();
        }


        List<ChildAlertDTO> response = children.stream()
                .map(child -> {
                    MedicalRecord mr = medicalRecordRepository.findByFirstNameAndLastName(child.getFirstName(), child.getLastName())
                            .orElseThrow(() -> new ErrorSystemException("Medical record not found : " + child.getFirstName() + " " + child.getLastName()));
                    return new ChildAlertDTO(child, personsAtAddress, mr);
                })
                .toList();

        log.info("Enfants trouvés à l'adresse: {}: {}", address, response.size());
        return response;
    }

    /**
     * Retourne les numéros de téléphone des personnes couvertes par une caserne donnée.
     *
     * @param fireStationNumber le numéro de la caserne.
     * @return une liste de numéros de téléphone distincts.
     * @throws IllegalArgumentException si le numéro est nul ou négatif.
     */
   @Override
    public List<String> getPhoneNumbersByFireStation(Integer fireStationNumber) {
       validateInteger(fireStationNumber);

       log.debug("Recherche des adresses couvertes par les casernes numéro: {}", fireStationNumber);
       List<String> addressesCovered = fireStationRepository.findAddressByNumberStation(fireStationNumber);

       if(addressesCovered.isEmpty()){
           log.info("Caserne introuvable avec ce numéro de station: {}", fireStationNumber);
           return Collections.emptyList();
       }

       log.debug("Recherche des adresses personnes couverte aux adresses: {}", addressesCovered);
       List<Person> personsAtAddresses = findPersonsAtAddresses(addressesCovered);

       log.debug("Recherche des numéros de téléphone des persons: {}", personsAtAddresses);
       List<String> phones = personsAtAddresses.stream()
               .map(Person::getPhone)
               .distinct()
               .toList();

       log.info("Numéros trouvés pour la caserne {}: {}", fireStationNumber, phones.size());
       return phones;
    }

    /**
     * Récupère une liste de personne habitant à une adresse, les MédicalsRecords liés
     * aux personnes et la FireStation les couvrants puis on instancie ResponseFireDTO avec ces
     * paramètres : ensuite le constructeur de ResponseFireDTO prend le relais.
     * @param address l'adresse à rechercher.
     * @return un {@link Optional} contenant un {@link ResponseFireDTO} avec les informations demandées.
     * @throws NotFoundException si aucune donnée n'est trouvée.
     * @throws ErrorSystemException si un dossier médical est manquant.
     * @throws IllegalArgumentException si l'adresse est vide ou nulle.
     */
    @Override
    public Optional<ResponseFireDTO> getPersonnesAndStationNumberByAddress(String address){
        validateString(address, "address");

        log.debug("Récupération des personnes pour l'adresse: {}", address);
        List<Person> personsAtAddress = repository.findByAddress(address);

        log.debug("récupération des dossiers médicaux de chaque personne de l'adresse: {}", address);
        List<MedicalRecord> medicalRecords = personsAtAddress.stream()
               .map(p -> medicalRecordRepository.findByFirstNameAndLastName(p.getFirstName(), p.getLastName()).orElseThrow(() -> {
                   log.error("Aucun dossier médical pour la personne : {}", p.getId());
                   return new ErrorSystemException("An error is occurred : Medical Record with name " + p.getId() + " not found");
               }))
               .toList();

        log.debug("récupération de la station à l'adresse: {}", address);
        Optional<FireStation> fireStationOpt = fireStationRepository.findByAddress(address);

        if(personsAtAddress.isEmpty() && fireStationOpt.isEmpty()){
            log.info("Aucune données pour l'adresse suivante: {}", address);
            throw new NotFoundException("Aucune Données n'as été trouvé pour l'adresse: " + address);
        }

        FireStation fireStation = fireStationOpt.orElse(null);


        log.info("Toutes Infos récupérées pour l'adresse: {}", address);
        return Optional.of(new ResponseFireDTO(personsAtAddress, medicalRecords, fireStation));
    }

    /**
     * Récupère les informations détaillées des foyers desservis par une ou plusieurs casernes (stations de pompiers).
     * <p>
     * Cette méthode est utilisée pour les alertes de type "Flood". Elle retourne, pour chaque foyer couvert par les
     * casernes indiquées, les occupants du foyer avec leurs informations personnelles et médicales.
     * </p>
     *
     * <p>Le traitement se fait en plusieurs étapes :</p>
     * <ol>
     *   <li>Récupération des adresses couvertes par les casernes dont les numéros sont donnés.</li>
     *   <li>Récupération des personnes vivant à ces adresses.</li>
     *   <li>Association des informations personnelles et médicales (âge, médicaments, allergies) à chaque personne.</li>
     *   <li>Groupement des personnes par adresse pour construire la structure attendue.</li>
     * </ol>
     *
     * @param fireStationNumbers liste des numéros de casernes (ex : 1, 2, 3).
     * @return une liste de {@link FloodResponseDTO}, chaque élément représentant un foyer avec :
     *         <ul>
     *             <li>l'adresse du foyer,</li>
     *             <li>la liste des occupants avec leurs données personnelles et médicales.</li>
     *         </ul>
     * @throws IllegalArgumentException si la liste est nulle, vide ou contient des éléments nulls.
     * @throws NotFoundException si aucune adresse n'est associée aux casernes, ou si aucune personne n'y est trouvée.
     */
     @Override
    public List<FloodResponseDTO> getPersonnesAndAddressByNumberFireStation(List<Integer> fireStationNumbers) {
        validateStationNumbers(fireStationNumbers);
        log.debug("Récupération des foyers pour les numéros de casernes: {}", fireStationNumbers);

        log.debug("Récupération des adresses par rapport au numéro de station {}", fireStationNumbers);
        List<String> addresses = findAddressesCoveredByStations(fireStationNumbers);

        // Si il n'y aucune adresse, ça sert a rien de continuer
        if(addresses.isEmpty()){
            log.info("Aucune donnée trouvé pour les numéros de station: {} ", fireStationNumbers);
            throw new NotFoundException("Aucune FireStations n'existe avec les numéros de station: " + fireStationNumbers);
        }

        log.debug("Récupération des personnes aux adresses: {}", addresses);
        List<Person> persons = findPersonsAtAddresses(addresses);

        log.debug("Regroupement des personnes par adresse: {}", addresses);
        Map<String, List<Person>> groupedPersons = groupPersonsByAddress(persons);

        // Retourne possiblement seulement des addresses de station avec des listes vides si personne n'habite à l'adresse de la fireStation trouvé
        List<FloodResponseDTO> response = buildFloodResponse(addresses, groupedPersons);
        log.info("Réponse flood générée pour {} adresses", addresses.size());
        return response;
    }

    /**
     * Récupère les informations détaillées des personnes partageant un même nom de famille.
     *
     * @param lastName le nom de famille recherché.
     * @return une liste de {@link PersonInfosLastNameDTO}.
     * @throws NotFoundException si aucune personne n'est trouvée.
     * @throws ErrorSystemException si un dossier médical est manquant.
     * @throws IllegalArgumentException si le nom est vide ou nul.
     */

    @Override
    public List<PersonInfosLastNameDTO>  getPersonsByLastName(String lastName) {
        validateString(lastName, "lastName");
        log.debug("Recherche des personnes avec le nom: {}", lastName);

        List<Person> persons = repository.findAllByLastName(lastName);

        if(persons.isEmpty()){
            log.info("Aucune personne trouvée avec le nom: {}", lastName);
            throw new NotFoundException("No Person found with lastName: " + lastName);
        }

        List<PersonInfosLastNameDTO> response = persons.stream()
                .map(p -> {
                    MedicalRecord mr = medicalRecordRepository.findByFirstNameAndLastName(p.getFirstName(), p.getLastName())
                            .orElseThrow(() -> {
                                log.error("Aucun dossier médical  pour la personne : {}", p.getId());
                                return new ErrorSystemException("Dossier médical introuvable pour: " + p.getId());
                            });
                    return new PersonInfosLastNameDTO(p, mr);
                })
                .toList();

        log.info("{} personnes trouvées avec le nom: {}", response.size(), lastName);
        return response;
    }

    /**
     * Retourne tous les emails uniques des personnes vivant dans une ville donnée.
     *
     * @param city le nom de la ville.
     * @return une liste d'emails.
     * @throws NotFoundException si aucun email n’est trouvé pour la ville.
     * @throws IllegalArgumentException si la ville est vide ou nulle.
     */
    @Override
    public List<String> getMailByCity(String city) {
        validateString(city, "city");
        log.debug("Recherche des emails pour la ville: {}", city);

        List<String> emailByCity = repository.getAll().stream()
                .filter(p -> Objects.equals(p.getCity().toLowerCase(),city.toLowerCase()))
                .map(Person::getEmail)
                .distinct()
                .toList();

        if(emailByCity.isEmpty()){
            log.info("Aucun email trouvé pour la ville: {}", city);
            throw new NotFoundException("No Email found with City: " + city);
        }

        log.info("{} emails trouvés pour la ville: {}", emailByCity.size(), city);
        return emailByCity;
    }

    //METHODE UTILITAIRE POUR VALIDER UN STRING
    private void validateString(String string, String messageParam) {
        Assert.hasText(string, messageParam + " must not be empty");
        Assert.notNull(string, messageParam + " must not be null");
    }

    private void validateInteger(Integer integer) {
        Assert.notNull(integer, "numberFireStation" + " must not be null");
        if (integer < 0) {
            throw new IllegalArgumentException("Number FireStation must not be negative");
        }
    }
    /**
     * Valide que la liste des numéros de casernes n'est ni nulle, ni vide,
     * et que chaque numéro est une chaîne non vide.
     *
     * @param numbers liste de numéros de casernes à valider
     * @throws IllegalArgumentException si la liste est nulle, vide ou contient des éléments vides
     */
    // METHODE UTILITAIRES POUR /FLOOD/FIRESTATIONS
    private void validateStationNumbers(List<Integer> numbers) {
        numbers.forEach(n -> Assert.notNull(n, "stationNumber must not be null"));
        Assert.notEmpty(numbers, "fireStationNumber list must not be empty");
    }


    /**
     * Récupère la liste des adresses couvertes par les numéros de casernes fournis.
     *
     * @param stationNumbers liste des numéros de casernes
     * @return liste distincte d'adresses couvertes par ces stations
     * @throws IllegalStateException si aucune adresse ne correspond aux stations
     */
    // METHODE UTILITAIRES POUR /FLOOD/FIRESTATIONS
    private List<String> findAddressesCoveredByStations(List<Integer> stationNumbers) {
        return stationNumbers.stream()
                .flatMap(n -> fireStationRepository.findAddressByNumberStation(n).stream())
                .distinct()
                .toList();
    }
    /**
     * Récupère toutes les personnes habitant aux adresses fournies.
     *
     * @param addresses liste d'adresses
     * @return liste des personnes vivant à ces adresses
     * @throws IllegalStateException si aucune personne n'est trouvée
     */
    // METHODE UTILITAIRES POUR /FLOOD/FIRESTATIONS
    private List<Person> findPersonsAtAddresses(List<String> addresses) {
        return repository.findByAddresses(addresses);
    }

    /**
     * Regroupe les personnes par adresse.
     *
     * @param persons liste des personnes à grouper
     * @return map associant chaque adresse à la liste des personnes qui y habitent
     */
    private Map<String, List<Person>> groupPersonsByAddress(List<Person> persons) {
        return persons.stream()
                .collect(Collectors.groupingBy(Person::getAddress));
    }

    /**
     * Construit la liste des DTO de réponse, en associant chaque adresse
     * à la liste des occupants avec leurs dossiers médicaux.
     *
     * @param addresses liste des adresses
     * @param personsByAddress map des personnes groupées par adresse
     * @return liste de {@link FloodResponseDTO} avec les informations groupées
     * @throws IllegalStateException si un dossier médical est introuvable pour une personne
     */
    // METHODE UTILITAIRES POUR /FLOOD/FIRESTATIONS
    private List<FloodResponseDTO> buildFloodResponse(List<String> addresses, Map<String, List<Person>> personsByAddress) {
        return addresses.stream()
                .map(address -> {
                    List<FloodResponseDTO.PersonInfoDTO> infos = personsByAddress
                            .getOrDefault(address, List.of())
                            .stream()
                            .map(p -> {
                                var mr = medicalRecordRepository
                                        .findByFirstNameAndLastName(p.getFirstName(), p.getLastName())
                                        .orElseThrow(() -> new ErrorSystemException("Une erreur est survenue : Dossier médical manquant pour : " + p.getId()));
                                return new FloodResponseDTO.PersonInfoDTO(p, mr);
                            })
                            .toList();
                    return new FloodResponseDTO(address, infos);
                })
                .toList();
    }
}
