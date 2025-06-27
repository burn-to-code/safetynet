package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.exception.ConflictException;
import com.safetynet.AppSafetyNet.exception.ErrorSystemException;
import com.safetynet.AppSafetyNet.exception.NotFoundException;
import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.model.dto.PersonCoveredDTO;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import com.safetynet.AppSafetyNet.service.Impl.FireStationServiceImpl;
import com.safetynet.AppSafetyNet.utils.ObjectFactoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FireStationServiceTest {
    @InjectMocks
    private FireStationServiceImpl service;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private FireStationRepository fireStationRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    private FireStation fireStation;
    @BeforeEach
    void setUp() {
        fireStation = ObjectFactoryTest.createFireStation("123 Main St", 1);
    }

    @Nested
    class PostFireStation {
        @Test
        void saveFireStation_shouldSave_whenNewFireStation() {
            FireStation newFS = ObjectFactoryTest.createFireStation("10 Rue Lafayette", 3);

            when(fireStationRepository.findByAddress("10 Rue Lafayette"))
                    .thenReturn(Optional.empty());

            service.saveFireStation(newFS);

            verify(fireStationRepository).saveFireStation(newFS);
        }

        @Test
        void saveFireStation_shouldThrowConflictException_whenAddressAlreadyExists() {
            FireStation existingFS = ObjectFactoryTest.createFireStation("10 Rue Lafayette", 3);

            when(fireStationRepository.findByAddress("10 Rue Lafayette"))
                    .thenReturn(Optional.of(existingFS));

            ConflictException ex = assertThrows(ConflictException.class, () -> service.saveFireStation(existingFS));

            assertEquals("FireStation already exists", ex.getMessage());
            verify(fireStationRepository, never()).saveFireStation(any());
        }

        @Test
        void saveFireStation_shouldThrowException_whenInputIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.saveFireStation(null));

            assertEquals("FireStation must not be null", ex.getMessage());
            verify(fireStationRepository, never()).saveFireStation(any());
        }
    }

    @Nested
    class PutFireStation {
        @Test
        void updateFireStation_shouldUpdate_whenExists() {
            FireStation updated = ObjectFactoryTest.createFireStation("123 Main St", 5);

            when(fireStationRepository.findByAddress("123 Main St"))
                    .thenReturn(Optional.of(fireStation));

            service.updateFireStation(updated);

            verify(fireStationRepository).saveFireStation(fireStation);
            assertEquals(5, fireStation.getStation());
        }

        @Test
        void updateFireStation_shouldThrowNotFound_whenNotExists() {
            when(fireStationRepository.findByAddress("123 Main St"))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> service.updateFireStation(fireStation));
            verify(fireStationRepository, never()).saveFireStation(any());
        }

        @Test
        void updateFireStation_shouldThrowException_whenNull() {
            assertThrows(IllegalArgumentException.class, () -> service.updateFireStation(null));
            verify(fireStationRepository, never()).saveFireStation(any());
        }
    }

    @Nested
    class DeleteFireStation {
        @Test
        void deleteFireStation_shouldDelete_whenExists() {
            when(fireStationRepository.findByAddress(fireStation.getAddress()))
                    .thenReturn(Optional.of(fireStation));

            service.deleteFireStation(fireStation.getAddress());

            verify(fireStationRepository, times(1)).deleteFireStation(fireStation);
        }

        @Test
        void deleteFireStation_shouldNotDelete_whenNotFound() {
            String address = "456 NotFound St";

            when(fireStationRepository.findByAddress(address))
                    .thenReturn(Optional.empty());

            service.deleteFireStation(address);

            verify(fireStationRepository, never()).deleteFireStation(any());
        }

        @Test
        void deleteFireStation_shouldThrowException_whenAddressIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.deleteFireStation(null));
            verify(fireStationRepository, never()).deleteFireStation(any());
        }
    }

    @Nested
    class GetPersonByNumberFireStation {

        @Test
        void getPersonCoveredByNumberStation_shouldReturnDTO_whenDataExists() {

            Person person = ObjectFactoryTest.createPerson("John", "Doe", fireStation.getAddress(), "City", "00000", "1234567890", "john@example.com");
            MedicalRecord mr = Mockito.mock(MedicalRecord.class);
            when(mr.isMajor()).thenReturn(true);  // simulons que John est majeur

            when(fireStationRepository.findAddressByNumberStation(fireStation.getStation())).thenReturn(List.of(fireStation.getAddress()));
            when(personRepository.findByAddresses(List.of(fireStation.getAddress()))).thenReturn(List.of(person));
            when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(Optional.of(mr));

            PersonCoveredDTO result = service.getPersonCoveredByNumberStation(fireStation.getStation());

            // Vérifie que la liste de PersonInfoDTO contient bien les infos de John Doe
            assertEquals(1, result.persons().size());
            PersonCoveredDTO.PersonInfoDTO personInfo = result.persons().getFirst();
            assertEquals("John", personInfo.firstName());
            assertEquals("Doe", personInfo.lastName());
            assertEquals("123 Main St 00000 City", personInfo.address()); // attention, c’est addressComplete
            assertEquals("1234567890", personInfo.phone());

            // Vérifie le comptage des majeurs/enfants
            assertEquals(1, result.adults());
            assertEquals(0, result.children());
        }

        @Test
        void getPersonCoveredByNumberStation_shouldThrowException_whenStationNumberIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.getPersonCoveredByNumberStation(null));
        }

        @Test
        void getPersonCoveredByNumberStation_shouldThrowNotFoundException_whenNoAddress() {
            when(fireStationRepository.findAddressByNumberStation(99)).thenReturn(Collections.emptyList());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> service.getPersonCoveredByNumberStation(99));

            assertEquals("Aucune FireStation avec le numéro de station : 99", exception.getMessage());
        }

        @Test
        void getPersonCoveredByNumberStation_shouldThrowErrorSystemException_whenMedicalRecordMissing() {
            Integer stationNumber = 1;
            String address = "123 Main St";
            Person person = ObjectFactoryTest.createPerson("Jane", "Doe", address, "City", "00000", "0987654321", "jane@example.com");

            when(fireStationRepository.findAddressByNumberStation(stationNumber)).thenReturn(List.of(address));
            when(personRepository.findByAddresses(List.of(address))).thenReturn(List.of(person));
            when(medicalRecordRepository.findByFirstNameAndLastName("Jane", "Doe")).thenReturn(Optional.empty());

            assertThrows(ErrorSystemException.class, () -> service.getPersonCoveredByNumberStation(stationNumber));
        }
    }
}

