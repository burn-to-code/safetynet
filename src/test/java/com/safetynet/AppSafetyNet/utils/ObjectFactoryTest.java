package com.safetynet.AppSafetyNet.utils;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;

import java.time.LocalDate;
import java.util.List;

public final class ObjectFactoryTest {

    private ObjectFactoryTest() {
    }

    public static FireStation createFireStation (String address, Integer StationNumber) {
        FireStation fs = new FireStation();
        fs.setAddress(address);
        fs.setStation(StationNumber);
        return fs;
    }

    public static Person createPerson (String firstName, String lastName, String address,  String city, String zip, String phone, String email) {
        Person p = new Person();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setAddress(address);
        p.setCity(city);
        p.setZip(zip);
        p.setPhone(phone);
        p.setEmail(email);
        return p;
    }

    public static MedicalRecord createMedicalRecord (String firstName, String lastName, LocalDate birthDate, List<String> medications, List<String> allergies) {
        MedicalRecord mr = new MedicalRecord();
        mr.setFirstName(firstName);
        mr.setLastName(lastName);
        mr.setBirthDate(birthDate);
        mr.setMedications(medications);
        mr.setAllergies(allergies);
        return mr;
    }
}
