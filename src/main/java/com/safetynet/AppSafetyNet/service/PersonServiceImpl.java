package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.ChildAlertDTO;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.model.UniqueEntity;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository repository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final FireStationRepository fireStationRepository;


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

    @Override
    public List<ChildAlertDTO> getChildrenByAddress(String address) {
        Assert.notNull(address, "Address must not be null");

        List<Person> personsAtAddress = repository.findByAddress(address);

        List<Person> children = personsAtAddress.stream()
                .filter(person -> {
                    MedicalRecord mr = medicalRecordRepository.getMedicalRecordByPerson(person.getFirstName(), person.getLastName());
                    return mr != null && !mr.isMajor();
                })
                .toList();

        return children.stream()
                .map(child -> {
                    MedicalRecord mr =  medicalRecordRepository.getMedicalRecordByPerson(child.getFirstName(), child.getLastName());
                    int age = mr.getAge();

                    List<String> PersonInSameHouse = personsAtAddress.stream()
                            .filter(p -> p.getLastName().equals(child.getLastName()) && !p.getFirstName().equals(child.getFirstName()))
                            .map(UniqueEntity::getId)
                            .toList();

                    return new ChildAlertDTO(child.getFirstName(), child.getLastName(), age, PersonInSameHouse);

                })
                .toList();
    }

   @Override
    public List<String> getPhoneNumbersByFireStation(String fireStationNumber) {
        List<String> addressesCovered = fireStationRepository.findAddressByNumberStation(fireStationNumber);

        List<Person> personsAtAddresses = repository.findByAddress(addressesCovered);

        return personsAtAddresses.stream()
                .map(Person::getPhone)
                .distinct()
                .toList();
    }
}
