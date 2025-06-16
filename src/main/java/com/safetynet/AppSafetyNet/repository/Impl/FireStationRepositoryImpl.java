package com.safetynet.AppSafetyNet.repository.Impl;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation concrète de l'interface FireStationRepository.
 * Utilise le service DataStorage pour manipuler les données de type FireStation.
 *
 * @see DataStorage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FireStationRepositoryImpl implements FireStationRepository {

    final private DataStorage dataStorage;

    /**
     * Retourne toutes les casernes enregistrées.
     */
    @Override
    public List<FireStation> getAll() {
        return dataStorage.getFireStations();
    }

    /**
     * Recherche une caserne à une adresse donnée.
     */
    @Override
    public Optional<FireStation> findByAddress(String address) {
        Assert.notNull(address, "Address of FireStation must not be null");
        return dataStorage.getFireStations()
                .stream()
                .filter(s -> s.getAddress().equalsIgnoreCase(address))
                .findFirst();
    }

    /**
     * Méthode qui permettrait de recupérer une fireStation si l'adresse etait complète : (rue + zip + ville)
    @Override
    public Optional<FireStation> findByAddress(String address) {
        Assert.notNull(address, "Address of FireStation must not be null");

        String[] parts = address.trim().split("\\s+");
        int len = parts.length;
        String[] addressWithoutZipAndCity = new String[len-2];
        System.arraycopy(parts, 0, addressWithoutZipAndCity, 0, len - 2);
        String addressForFireStations = String.join(" ", addressWithoutZipAndCity);

        Optional<FireStation> fireStationOp =  dataStorage.getFireStations()
                .stream()
                .filter(s -> s.getAddress().equals(addressForFireStations))
                .findFirst();

        if (fireStationOp.isEmpty()) {
            log.warn("No FireStation found for address '{}'", addressForFireStations);
            return Optional.empty();
        }

        return fireStationOp;
    }
    */


    /**
     * Enregistre ou remplace une caserne associée à une adresse.
     */
    @Override
    public void saveFireStation(FireStation fs) {
        Assert.notNull(fs, "FireStation must not be null");
        dataStorage.getFireStations().removeIf(f -> f.getAddress().equals(fs.getAddress()));
        dataStorage.getFireStations().add(fs);
        dataStorage.saveData();
        log.info("FireStation saved : {} {}", fs.getAddress(), fs.getStation());
    }

    /**
     * Supprime une association adresse/station.
     */
    @Override
    public void deleteFireStation(FireStation fs) {
        Assert.notNull(fs, "FireStation must not be null");
        dataStorage.getFireStations().remove(fs);
        dataStorage.saveData();
        log.info("FireStation deleted : {} {}", fs.getAddress(), fs.getStation());
    }


    /**
     * Retourne les adresses associées à une station de pompiers donnée.
     */
    @Override
    public List<String> findAddressByNumberStation(String number) {
        Assert.notNull(number, "Number Station must not be null");
        return dataStorage.getFireStations()
                .stream()
                .filter(fs -> fs.getStation().equals(number))
                .map(FireStation::getAddress)
                .collect(Collectors.toList());
    }
}
