package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository repository;

    @Override
    public void addPerson(Person person) {
        Assert.notNull(person, "Person must not be null");

        if (repository.findByFirstNameAndLastName(person.getFirstName(), person.getLastName())
                .isPresent()){
            throw new IllegalArgumentException("Person already exists");
        }
        repository.save(person);
    }

    @Override
    public void removePerson(String firstName, String lastName) {
        Assert.notNull(firstName, "First name must not be null");
        Assert.notNull(lastName, "Last name must not be null");
        repository.findByFirstNameAndLastName(firstName, lastName)
                .ifPresent(repository::delete);
    }

    @Override
    public void updatePerson(Person person) {
        Assert.notNull(person, "Person must not be null");

        Person personToUpdate = repository.findByFirstNameAndLastName(person.getFirstName(), person.getLastName())
                .orElseThrow(() -> new IllegalArgumentException("Person not found with id " + person.getId()));

        personToUpdate.setCity(person.getCity());
        personToUpdate.setAddress(person.getAddress());
        personToUpdate.setEmail(person.getEmail());
        personToUpdate.setPhone(person.getPhone());

        repository.save(personToUpdate);
    }
}
