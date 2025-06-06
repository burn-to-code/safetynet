package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataWrapper {
    private List<Person> persons = new ArrayList<>();
    private List<MedicalRecord>  medicalrecords  = new ArrayList<>();
    private List<FireStation>  firestations  = new ArrayList<>();
}
