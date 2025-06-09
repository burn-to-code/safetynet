package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.ChildAlertDTO;
import com.safetynet.AppSafetyNet.model.Person;

import java.util.List;

public interface PersonService {

    void addPerson(Person person);
    void removePerson(String firstName, String lastName);
    void updatePerson(Person person);
    List<ChildAlertDTO> getChildrenByAddress(String address);
    List<String> getPhoneNumbersByFireStation(String fireStationNumber);

}
