package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.dto.ChildAlertDTO;
import com.safetynet.AppSafetyNet.model.dto.FloodResponseDTO;
import com.safetynet.AppSafetyNet.model.dto.PersonInfosLastNameDTO;
import com.safetynet.AppSafetyNet.model.dto.ResponseFireDTO;
import com.safetynet.AppSafetyNet.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les alertes relatives aux enfants et aux numéros de téléphone
 * selon des critères d'adresse ou de numéro de caserne de pompiers.
 */
@Slf4j
@RestController
public class AlertController {

    private final PersonService personService;

    /**
     * Constructeur avec injection du service PersonService.
     *
     * @param personService service métier pour gérer les personnes.
     */
    public AlertController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Récupère la liste des enfants résidant à une adresse donnée,
     * accompagnés des autres membres du foyer.
     *
     * @param address L’adresse postale à interroger (ex. : "1509 Culver St").
     * @return HTTP 200 avec une liste de {@link ChildAlertDTO}, ou chaîne vide si aucun enfant.
     */
    @GetMapping("/childAlert")
    public ResponseEntity<?> getChildrenAtAddress(@RequestParam String address) {
        log.info("Requête GET /childAlert reçue avec address={}", address);
        var children = personService.getChildrenByAddress(address);
        if (children.isEmpty()) {
            log.info("Aucun enfant trouvé pour l'adresse {}", address);
            return ResponseEntity.ok("");
        }
        log.info("Enfants trouvés pour l'adresse {} : {}", address, children.size());
        return ResponseEntity.ok(children);
    }

    /**
     * Récupère les numéros de téléphone des personnes couvertes par une station de pompiers.
     *
     * @param numberFireStation Le numéro de la station (ex. : 3).
     * @return HTTP 200 avec une liste de numéros, ou chaîne vide si aucune correspondance.
     */
    @GetMapping("/phoneAlert")
    public ResponseEntity<?> getPhoneAtAddress(@RequestParam Integer numberFireStation) {
        log.info("Requête GET /phoneAlert reçue avec numberFireStation={}", numberFireStation);
        List<String> listOfPhone= personService.getPhoneNumbersByFireStation(numberFireStation);
        if (listOfPhone.isEmpty()) {
            return ResponseEntity.ok("");
        }
        log.info("Liste de {} numéros de téléphone retournée pour la station {}", listOfPhone.size(), numberFireStation);
        return ResponseEntity.ok(listOfPhone);
    }

    /**
     * Récupère une liste de personnes (nom, prénom, adresse et téléphone + medicament et allergies)
     * par adresse ainsi que le numéro de la FireStation couvrant cette adresse
     * @param address une adresse postale
     * @return une réponse HTTP 200 avec la liste des personnes et le numéro de station.
     */
    @GetMapping("/fire")
    public ResponseEntity<?> getFireAtAddress(@RequestParam String address) {
        log.info("Requête GET /fire reçue avec address={}", address);
        Optional<ResponseFireDTO> response = personService.getPersonnesAndStationNumberByAddress(address);
        log.info("Réponse fire retournée pour address={}", address);
        return ResponseEntity.ok(response);
    }

    /**
     * Fournit la liste des foyers couverts par une ou plusieurs casernes.
     * <p>
     * Chaque foyer contient :
     * <ul>
     *     <li>L'adresse,</li>
     *     <li>Les habitants,</li>
     *     <li>Leurs informations personnelles et médicales.</li>
     * </ul>
     * </p>
     *
     * @param stationNumber Liste des numéros de casernes à interroger (ex. : [1, 2]).
     * @return HTTP 200 avec une liste de {@link FloodResponseDTO}.
     */
    @GetMapping("/flood/stations")
    public ResponseEntity<?> getFloodAtAddress(@RequestParam List<Integer> stationNumber) {
        log.info("Requête GET /flood/stations reçue avec stationNumber={}", stationNumber);
        List<FloodResponseDTO> response = personService.getPersonnesAndAddressByNumberFireStation(stationNumber);
        log.info("Réponse flood retournée avec {} entrées", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les informations détaillées (adresse, email, téléphone, âge, traitements)
     * des personnes partageant un même nom de famille.
     *
     * @param lastName Le nom de famille (ex. : "Boyd").
     * @return HTTP 200 avec une liste de {@link PersonInfosLastNameDTO}.
     */
    @GetMapping("/personInfoLastName")
    public ResponseEntity<?> getPersonInfoLastName(@RequestParam String lastName) {
        log.info("Requête GET /personInfoLastName reçue avec lastName={}", lastName);
        List<PersonInfosLastNameDTO> response = personService.getPersonsByLastName(lastName);
        log.info("Liste des personnes avec nom {} retournée ({} entrées)", lastName, response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les emails uniques de toutes les personnes résidant dans une ville donnée.
     *
     * @param city Le nom de la ville (ex. : "Culver").
     * @return HTTP 200 avec une liste d’emails.
     */
    @GetMapping("/communityEmail")
    public ResponseEntity<?> getCommunityEmailByCity(@RequestParam String city) {
        log.info("Requête GET /communityEmail reçue avec city={}", city);
        List<String> emails = personService.getMailByCity(city);
        log.info("Liste des emails retournée pour la ville {} ({} emails)", city, emails.size());
        return ResponseEntity.ok(emails);
    }
}
