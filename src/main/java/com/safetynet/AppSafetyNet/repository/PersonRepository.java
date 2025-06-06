package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    List<Person> getAll();
    Optional<Person> findByFirstNameAndLastName(String firstName, String lastName);

    void save(Person person);

    void delete(Person person);

}
