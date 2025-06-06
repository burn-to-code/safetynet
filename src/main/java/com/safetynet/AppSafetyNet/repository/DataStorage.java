package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;

import java.io.IOException;
import java.util.List;

public interface DataStorage {
    void initializeDataFile()  throws IOException;
    void loadData()  throws IOException;
    void saveData();
    List<Person> getPersons();
    List<FireStation>  getFireStations();
    List<MedicalRecord> getMedicalRecords();
}
