package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.dto.FloodResponseDTO;
import com.safetynet.AppSafetyNet.model.dto.PersonInfosLastNameDTO;
import com.safetynet.AppSafetyNet.model.dto.ResponseFireDTO;
import com.safetynet.AppSafetyNet.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
     * Récupère la liste des enfants vivant à une adresse donnée,
     * ainsi que les autres personnes habitant à la même adresse.
     *
     * @param address Adresse à interroger (paramètre de requête).
     * @return Une réponse HTTP 200 avec la liste des enfants et leurs cohabitants,
     * ou une chaîne vide si aucun enfant n'est trouvé.
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
     * Récupère la liste des numéros de téléphone des personnes couvertes
     * par une station de pompiers donnée.
     *
     * @param numberFireStation Numéro de la station de pompiers (paramètre de requête).
     * @return Une réponse HTTP 200 avec la liste des numéros de téléphone.
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

    @GetMapping("/flood/stations")
    public ResponseEntity<?> getFloodAtAddress(@RequestParam List<Integer> stationNumber) {
        log.info("Requête GET /flood/stations reçue avec stationNumber={}", stationNumber);
        List<FloodResponseDTO> response = personService.getPersonnesAndAddressByNumberFireStation(stationNumber);
        log.info("Réponse flood retournée avec {} entrées", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/personInfoLastName")
    public ResponseEntity<?> getPersonInfoLastName(@RequestParam String lastName) {
        log.info("Requête GET /personInfoLastName reçue avec lastName={}", lastName);
        List<PersonInfosLastNameDTO> response = personService.getPersonsByLastName(lastName);
        log.info("Liste des personnes avec nom {} retournée ({} entrées)", lastName, response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/communityEmail")
    public ResponseEntity<?> getCommunityEmailByCity(@RequestParam String city) {
        log.info("Requête GET /communityEmail reçue avec city={}", city);
        List<String> emails = personService.getMailByCity(city);
        log.info("Liste des emails retournée pour la ville {} ({} emails)", city, emails.size());
        return ResponseEntity.ok(emails);
    }
}
