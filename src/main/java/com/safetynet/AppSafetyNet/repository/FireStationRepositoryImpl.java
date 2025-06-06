package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.FireStation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FireStationRepositoryImpl implements FireStationRepository {

    final private DataStorage dataStorage;

    @Override
    public List<FireStation> getAll() {
        return dataStorage.getFireStations();
    }

    @Override
    public Optional<FireStation> findByAddress(String address) {
        return dataStorage.getFireStations()
                .stream()
                .filter(s -> s.getAddress().equals(address))
                .findFirst();
    }

    @Override
    public void saveFireStation(FireStation fs) {
        dataStorage.getFireStations().removeIf(f -> f.getAddress().equals(fs.getAddress()));
        dataStorage.getFireStations().add(fs);
        dataStorage.saveData();
    }

    @Override
    public void deleteFireStation(FireStation fs) {
        dataStorage.getFireStations().remove(fs);
        dataStorage.saveData();
    }

    @Override
    public List<String> findAddressByNumberStation(String number) {
        return dataStorage.getFireStations()
                .stream()
                .filter(fs -> fs.getStation().equals(number))
                .map(FireStation::getAddress)
                .collect(Collectors.toList());
    }
}
