package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.model.*;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FireStationServiceImpl implements FireStationService {

    private final FireStationRepository fireStationRepository;
    private final PersonRepository personRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Override
    public void saveFireStation (FireStation fireStation) {
        Assert.notNull(fireStation,  "FireStation must not be null");

        // pour éviter les doublons
        if (fireStationRepository.findByAddress(fireStation.getAddress()).isPresent()) {
            throw new IllegalStateException("FireStation already exists");
        }

        fireStationRepository.saveFireStation(fireStation);
        log.info("FireStation saved successfully");
    }

    @Override
    public void updateFireStation (FireStation updatedFireStation) {
        Assert.notNull(updatedFireStation,  "FireStation must not be null");

        FireStation fs = fireStationRepository.findByAddress(updatedFireStation.getAddress())
                .orElseThrow(() -> new IllegalStateException("FireStation does not exist"));

        fs.setStation(updatedFireStation.getStation());

        fireStationRepository.saveFireStation(fs);
        log.info("FireStation updated successfully");
    }

    @Override
    public void deleteFireStation (String address) {
        Assert.notNull(address, "FireStation must not be null");
        FireStation fs = fireStationRepository.findByAddress(address)
                .orElseThrow(() -> new IllegalStateException("FireStation does not exist"));

        fireStationRepository.deleteFireStation(fs);
        log.info("FireStation has been deleted");
    }

    @Override
    public PersonCoveredDTO getPersonCoveredByNumberStation(String stationNumber) {
        List<String> address= fireStationRepository.findAddressByNumberStation(stationNumber);
        List<Person> persons= personRepository.findByAddress(address);
        List<MedicalRecord> medicalRecords = persons.stream()
                .map(p -> medicalRecordRepository.getMedicalRecordByPerson(p.getFirstName(), p.getLastName()))
                .toList();

        List<PersonInfoDTO> result = persons.stream()
                .map(p -> new PersonInfoDTO(p.getFirstName(), p.getLastName(), p.getAddress(), p.getPhone()))
                .toList();

        int adults = (int) medicalRecords.stream()
                .filter(MedicalRecord::isMajor)
                .count();

        int children = medicalRecords.size() - adults;


        return new PersonCoveredDTO(result, adults, children);
    }
}
