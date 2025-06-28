package com.safetynet.AppSafetyNet.service.Impl;

import com.safetynet.AppSafetyNet.exception.ConflictException;
import com.safetynet.AppSafetyNet.exception.ErrorSystemException;
import com.safetynet.AppSafetyNet.exception.NotFoundException;
import com.safetynet.AppSafetyNet.model.*;
import com.safetynet.AppSafetyNet.model.dto.PersonCoveredDTO;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import com.safetynet.AppSafetyNet.service.FireStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Implémentation du service de gestion des casernes de pompiers.
 * Cette classe effectue les vérifications de base et délègue les opérations
 * de persistance aux repositories associés.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FireStationServiceImpl implements FireStationService {

    private final FireStationRepository fireStationRepository;
    private final PersonRepository personRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Ajoute une nouvelle caserne de pompiers, à condition qu’elle n’existe pas déjà.
     *
     * @param fireStation L’objet {@link FireStation} à sauvegarder.
     * @throws IllegalArgumentException si l’objet est null.
     * @throws ConflictException si une caserne existe déjà à cette adresse.
     */
    @Override
    public void saveFireStation (FireStation fireStation) {
        log.debug("Tentative de sauvegarde de FireStation: {}", fireStation);
        Assert.notNull(fireStation,  "FireStation must not be null");

        // pour éviter les doublons
        if (fireStationRepository.findByAddress(fireStation.getAddress()).isPresent()) {
            log.info("FireStation déjà existante à l'adresse : {}", fireStation.getAddress());
            throw new ConflictException("FireStation already exists");
        }

        fireStationRepository.saveFireStation(fireStation);
        log.info("FireStation sauvegardée avec succès à l'adresse et au numéro de caserne : {}, {}", fireStation.getAddress(), fireStation.getStation());
    }

    /**
     * Met à jour le numéro de station d’une caserne existante, identifiée par son adresse.
     *
     * @param updatedFireStation L’objet contenant l’adresse cible et le nouveau numéro de station.
     * @throws IllegalArgumentException si l’objet est null.
     * @throws NotFoundException si aucune caserne n’existe à cette adresse.
     */
    @Override
    public void updateFireStation (FireStation updatedFireStation) {
        Assert.notNull(updatedFireStation,  "FireStation must not be null");
        log.debug("Tentative de mise à jour de FireStation: {}", updatedFireStation);

        FireStation fs = fireStationRepository.findByAddress(updatedFireStation.getAddress())
                .orElseThrow(() -> {
                    log.error("Modification impossible : FireStation inexistante à l'adresse : {}", updatedFireStation.getAddress());
                    return new NotFoundException("FireStation does not exist");
                });

        fs.setStation(updatedFireStation.getStation());

        fireStationRepository.saveFireStation(fs);
        log.info("FireStation mise à jour avec succès à l'adresse et numéro de station : {}, {}", fs.getAddress(), fs.getStation());
    }

    /**
     * Supprime une caserne à partir de son adresse, si elle existe.
     *
     * @param address L’adresse de la caserne à supprimer.
     * @throws IllegalArgumentException si l’adresse est null.
     */
    @Override
    public void deleteFireStation (String address) {
        log.debug("Tentative de suppression de FireStation à l'adresse : {}", address);
        Assert.notNull(address, "FireStation must not be null");

        fireStationRepository.findByAddress(address)
                .ifPresent(fireStationRepository::deleteFireStation);
    }

    /**
     * Récupère toutes les personnes couvertes par une station donnée,
     * ainsi que leurs dossiers médicaux.
     * <p>
     * Le résultat contient :
     * <ul>
     *     <li>Les données personnelles des personnes (nom, prénom, adresse, téléphone),</li>
     *     <li>Leurs dossiers médicaux (âge, médicaments, allergies),</li>
     *     <li>Le nombre d’adultes et d’enfants (calculé dans le DTO {@link PersonCoveredDTO}).</li>
     * </ul>
     *
     * @param stationNumber Le numéro de la station de pompiers.
     * @return Un {@link PersonCoveredDTO} avec les données agrégées.
     * @throws IllegalArgumentException si le numéro de station est null.
     * @throws NotFoundException si aucune adresse ne correspond à ce numéro de station.
     * @throws ErrorSystemException si un dossier médical est manquant pour une personne.
     */
    @Override
    public PersonCoveredDTO getPersonCoveredByNumberStation(Integer stationNumber) {
        log.debug("Récupération des personnes couvertes pour la station numéro : {}", stationNumber);
        Assert.notNull(stationNumber, "FireStation must not be null");

        List<String> address= fireStationRepository.findAddressByNumberStation(stationNumber);
        if (address.isEmpty()) {
            throw new NotFoundException("Aucune FireStation avec le numéro de station : "+ stationNumber);
        }
        List<Person> persons= personRepository.findByAddresses(address);
        List<MedicalRecord> medicalRecords = persons.stream()
                .map(p -> medicalRecordRepository.findByFirstNameAndLastName(p.getFirstName(), p.getLastName())
                        .orElseThrow(()  -> new ErrorSystemException("Medical record not found for: " + p.getId())))
                .toList();

        log.info("Récupération réussie des personnes couvertes pour la station numéro : {} ({} personnes)", stationNumber, persons.size());
        return new PersonCoveredDTO(persons, medicalRecords);
    }
}
