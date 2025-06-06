package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.Person;

public interface PersonService {

    void addPerson(Person person);
    void removePerson(String firstName, String lastName);
    void updatePerson(Person person);

}
