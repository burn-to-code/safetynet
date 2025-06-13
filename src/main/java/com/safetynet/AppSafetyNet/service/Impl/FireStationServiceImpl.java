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
        Assert.notNull(fireStation,  "FireStation must not be null");

        // pour éviter les doublons
        if (fireStationRepository.findByAddress(fireStation.getAddress()).isPresent()) {
            throw new IllegalStateException("FireStation already exists");
        }

        fireStationRepository.saveFireStation(fireStation);
        log.info("FireStation saved successfully");
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
        Assert.notNull(updatedFireStation,  "FireStation must not be null");

        FireStation fs = fireStationRepository.findByAddress(updatedFireStation.getAddress())
                .orElseThrow(() -> new IllegalStateException("FireStation does not exist"));

        fs.setStation(updatedFireStation.getStation());

        fireStationRepository.saveFireStation(fs);
        log.info("FireStation updated successfully");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supprime une caserne identifiée par son adresse après vérification.
     * </p>
     */
    @Override
    public void deleteFireStation (String address) {
        Assert.notNull(address, "FireStation must not be null");
        FireStation fs = fireStationRepository.findByAddress(address)
                .orElseThrow(() -> new IllegalStateException("FireStation does not exist"));

        fireStationRepository.deleteFireStation(fs);
        log.info("FireStation has been deleted");
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

        if (!stationNumber.matches("\\d+")) {
            throw new IllegalArgumentException("La chaîne '" + stationNumber + "' doit contenir uniquement des chiffres.");
        }

        List<String> address= fireStationRepository.findAddressByNumberStation(stationNumber);

        if (address.isEmpty()) {
            throw new IllegalArgumentException("Aucune adresse trouvée pour la caserne numéro " + stationNumber);
        }

        List<Person> persons= personRepository.findByAddresses(address);
        List<MedicalRecord> medicalRecords = persons.stream()
                .map(p -> medicalRecordRepository.getMedicalRecordByPerson(p.getFirstName(), p.getLastName()))
                .toList();
        return new PersonCoveredDTO(persons, medicalRecords);
    }
}
