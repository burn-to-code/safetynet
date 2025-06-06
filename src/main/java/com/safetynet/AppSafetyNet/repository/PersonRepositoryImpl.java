package com.safetynet.AppSafetyNet.repository;

import com.safetynet.AppSafetyNet.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonRepositoryImpl implements PersonRepository {

    final private DataStorage dataStorageService;

    @Override
    public List<Person> getAll() {
        return dataStorageService.getPersons();
    }

    @Override
    public Optional<Person> findByFirstNameAndLastName(String firstName, String lastName) {
        return dataStorageService.getPersons()
                .stream()
                .filter(p -> p.getFirstName().equals(firstName))
                .filter(p -> p.getLastName().equals(lastName))
                .findFirst();
    }

    @Override
    public void save(Person person) {
        dataStorageService.getPersons().add(person);
        dataStorageService.saveData();
    }

    @Override
    public void delete(Person person) {
        dataStorageService.getPersons().remove(person);
    }
}
