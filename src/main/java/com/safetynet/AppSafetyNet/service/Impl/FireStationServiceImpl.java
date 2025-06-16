package com.safetynet.AppSafetyNet.service.Impl;

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
     * {@inheritDoc}
     */
    @Override
    public void saveFireStation (FireStation fireStation) {
        log.debug("Tentative de sauvegarde de FireStation: {}", fireStation);
        Assert.notNull(fireStation,  "FireStation must not be null");

        // pour éviter les doublons
        if (fireStationRepository.findByAddress(fireStation.getAddress()).isPresent()) {
            log.error("FireStation déjà existante à l'adresse : {}", fireStation.getAddress());
            throw new IllegalStateException("FireStation already exists");
        }

        fireStationRepository.saveFireStation(fireStation);
        log.info("FireStation sauvegardée avec succès à l'adresse et au numéro de caserne : {}, {}", fireStation.getAddress(), fireStation.getStation());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Met à jour le numéro de station d'une caserne existante,
     * identifiée par son adresse.
     * </p>
     */
    @Override
    public void updateFireStation (FireStation updatedFireStation) {
        log.debug("Tentative de mise à jour de FireStation: {}", updatedFireStation);
        Assert.notNull(updatedFireStation,  "FireStation must not be null");

        FireStation fs = fireStationRepository.findByAddress(updatedFireStation.getAddress())
                .orElseThrow(() -> {
                    log.error("Modification impossible : FireStation inexistante à l'adresse : {}", updatedFireStation.getAddress());
                    return new IllegalStateException("FireStation does not exist");
                });

        fs.setStation(updatedFireStation.getStation());

        fireStationRepository.saveFireStation(fs);
        log.info("FireStation mise à jour avec succès à l'adresse et numéro de station : {}, {}", fs.getAddress(), fs.getStation());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supprime une caserne identifiée par son adresse après vérification.
     * </p>
     */
    @Override
    public void deleteFireStation (String address) {
        log.debug("Tentative de suppression de FireStation à l'adresse : {}", address);
        Assert.notNull(address, "FireStation must not be null");
        FireStation fs = fireStationRepository.findByAddress(address)
                .orElseThrow(() -> {
                    log.error("Suppression impossible : FireStation inexistante à l'adresse : {}", address);
                    return new IllegalStateException("FireStation does not exist");
                });

        fireStationRepository.deleteFireStation(fs);
        log.info("FireStation supprimée avec succès à l'adresse et au numéro de station : {}, {}", address, fs.getStation());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Récupère les personnes couvertes par le numéro de caserne,
     * avec leurs informations personnelles, ainsi que le nombre d’adultes et d’enfants.
     * </p>
     */
    @Override
    public PersonCoveredDTO getPersonCoveredByNumberStation(String stationNumber) {
        log.debug("Récupération des personnes couvertes pour la station numéro : {}", stationNumber);
        Assert.notNull(stationNumber, "FireStation must not be null");

        if (!stationNumber.matches("\\d+")) {
            log.error("Numéro de station invalide reçu : {}", stationNumber);
            throw new IllegalArgumentException("La chaîne '" + stationNumber + "' doit contenir uniquement des chiffres.");
        }

        List<String> address= fireStationRepository.findAddressByNumberStation(stationNumber);

        if (address.isEmpty()) {
            log.error("Aucune adresse trouvée pour la station numéro : {}", stationNumber);
            throw new IllegalStateException("Aucune adresse trouvée pour la caserne numéro " + stationNumber + " ce numéro de station ne doit pas exister");
        }

        List<Person> persons= personRepository.findByAddresses(address);
        List<MedicalRecord> medicalRecords = persons.stream()
                .map(p -> medicalRecordRepository.getMedicalRecordByPerson(p.getFirstName(), p.getLastName()))
                .toList();

        log.info("Récupération réussie des personnes couvertes pour la station numéro : {} ({} personnes)", stationNumber, persons.size());

        return new PersonCoveredDTO(persons, medicalRecords);
    }
}
