package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.dto.ResponseFireDTO;
import com.safetynet.AppSafetyNet.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * Contrôleur REST pour gérer les alertes relatives aux enfants et aux numéros de téléphone
 * selon des critères d'adresse ou de numéro de caserne de pompiers.
 */
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
        var children = personService.getChildrenByAddress(address);
        if (children.isEmpty()) {
            return ResponseEntity.ok("");
        }
        return ResponseEntity.ok(children);
    }


    /**
     * Récupère la liste des numéros de téléphone des personnes couvertes
     * par une station de pompiers donnée.
     *
     * @param fireStation Numéro de la station de pompiers (paramètre de requête).
     * @return Une réponse HTTP 200 avec la liste des numéros de téléphone.
     */
    @GetMapping("/phoneAlert")
    public ResponseEntity<?> getPhoneAtAddress(@RequestParam String fireStation) {
        List<String> listOfPhone= personService.getPhoneNumbersByFireStation(fireStation);
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
        ResponseFireDTO response = personService.getPersonnesAndStationNumberByAddress(address);
        return ResponseEntity.ok(response);
    }
}
