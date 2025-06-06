package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.PersonCoveredDTO;

public interface FireStationService {
    void saveFireStation (FireStation fireStation);
    void updateFireStation (FireStation updatedFireStation);
    void deleteFireStation (String address);
    PersonCoveredDTO getPersonCoveredByNumberStation(String stationNumber);
}
