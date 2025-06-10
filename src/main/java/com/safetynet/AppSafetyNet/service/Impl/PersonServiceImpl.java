package com.safetynet.AppSafetyNet.service.Impl;

import com.safetynet.AppSafetyNet.model.*;
import com.safetynet.AppSafetyNet.model.dto.ChildAlertDTO;
import com.safetynet.AppSafetyNet.model.dto.ResponseFireDTO;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import com.safetynet.AppSafetyNet.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository repository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final FireStationRepository fireStationRepository;


    /**
     * {@inheritDoc}
     */
    @Override
    public void addPerson(Person person) {
        Assert.notNull(person, "Person must not be null");

        if (repository.findByFirstNameAndLastName(person.getFirstName(), person.getLastName())
                .isPresent()){
            throw new IllegalArgumentException("Person already exists");
        }
        repository.save(person);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePerson(String firstName, String lastName) {
        Assert.notNull(firstName, "First name must not be null");
        Assert.notNull(lastName, "Last name must not be null");
        repository.findByFirstNameAndLastName(firstName, lastName)
                .ifPresent(repository::delete);
    }


    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Recherche les personnes vivant à l'adresse donnée,
     * filtre celles qui sont mineures selon leur fiche médicale,
     * puis construit une liste DTO contenant les enfants avec leur âge
     * et les autres membres du foyer.
     * </p>
     */
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
                    return new ChildAlertDTO(child, personsAtAddress, mr);})
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Récupère les adresses couvertes par une caserne,
     * puis les personnes vivant à ces adresses,
     * enfin retourne les numéros de téléphone distincts.
     * </p>
     */
   @Override
    public List<String> getPhoneNumbersByFireStation(String fireStationNumber) {
        List<String> addressesCovered = fireStationRepository.findAddressByNumberStation(fireStationNumber);

        List<Person> personsAtAddresses = repository.findByAddress(addressesCovered);

        return personsAtAddresses.stream()
                .map(Person::getPhone)
                .distinct()
                .toList();
    }

    /**
     * Récupère une liste de personne habitant à une adresse, les MédicalsRecords liés
     * aux personnes et la FireStation les couvrants puis on instancie ResponseFireDTO avec ces
     * paramètres : ensuite le constructeur de ResponseFireDTO prend le relais.
     * @param address une adresse postale
     * @return Un DTO sous la forme de réponse attendu, c'est-à-dire : le nom, le
     * numéro de téléphone, l'âge et les antécédents médicaux. + le numéro de la FireStation.
     */
    @Override
    public ResponseFireDTO getPersonnesAndStationNumberByAddress(String address){
       List<Person> personsAtAddress = repository.findByAddress(address);
       List<MedicalRecord> medicalRecords = personsAtAddress.stream()
               .map(p -> medicalRecordRepository.getMedicalRecordByPerson(p.getFirstName(), p.getLastName()))
               .toList();
       FireStation fireStation = fireStationRepository.findByAddress(address).orElseThrow(() -> new IllegalArgumentException("Fire station not found with id " + address));
       return new ResponseFireDTO(personsAtAddress, medicalRecords, fireStation);
    }
}
