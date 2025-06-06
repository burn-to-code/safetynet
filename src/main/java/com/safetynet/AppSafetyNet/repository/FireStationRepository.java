package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.FireStation;

import java.util.List;
import java.util.Optional;

public interface FireStationRepository {
    List<FireStation> getAll();
    Optional<FireStation> findByAddress(String address);
    void saveFireStation(FireStation fs);
    void deleteFireStation(FireStation fs);
    List<String> findAddressByNumberStation(String number);
}
