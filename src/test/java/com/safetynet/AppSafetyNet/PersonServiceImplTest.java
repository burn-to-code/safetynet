package com.safetynet.AppSafetyNet;

import com.safetynet.AppSafetyNet.exception.ErrorSystemException;
import com.safetynet.AppSafetyNet.exception.NotFoundException;
import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.dto.*;
import com.safetynet.AppSafetyNet.exception.ConflictException;
import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import com.safetynet.AppSafetyNet.service.Impl.PersonServiceImpl;
import com.safetynet.AppSafetyNet.utils.ObjectFactoryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonServiceImplTest {
    @InjectMocks
    private PersonServiceImpl service;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private FireStationRepository fireStationRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    // ----------------------------------------------------------------------------------
    // #region: CRUD Operations
    // ----------------------------------------------------------------------------------

    @Nested
    class AddPerson {
        @Test
        void shouldSaveNewPerson() {
            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", "100 Street City", "Miami", "00000", "999-999-666", "steve@example.com");
            when(personRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.empty());

            service.addPerson(person);

            verify(personRepository).save(person);
        }

        @Test
        void shouldThrowConflict_whenPersonExists() {
            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", "100 Street City", "Miami", "00000", "999-999-666", "steve@example.com");
            when(personRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.of(person));

            assertThrows(ConflictException.class, () -> service.addPerson(person));
            verify(personRepository, never()).save(any());
        }

        @Test
        void shouldThrowException_whenPersonIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addPerson(null));
            assertEquals("Person must not be null", ex.getMessage());
            verify(personRepository, never()).save(any());
        }
    }

    @Nested
    class UpdatePerson {
        @Test
        void shouldUpdateExistingPerson() {
            Person input = ObjectFactoryTest.createPerson("Steve", "Wonder", "New Addr", "City", "00000", "999", "email@example.com");
            Person existing = ObjectFactoryTest.createPerson("Steve", "Wonder", "Old Addr", "Old", "11111", "111", "old@example.com");
            when(personRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.of(existing));

            service.updatePerson(input);

            verify(personRepository).save(existing);
        }

        @Test
        void shouldThrowNotFound_whenPersonNotExist() {
            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", "New Addr", "City", "00000", "999", "email@example.com");
            when(personRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () -> service.updatePerson(person));
            assertEquals("Person not found : " + person.getId(), ex.getMessage());
        }

        @Test
        void shouldThrowException_whenPersonNull() {
            assertThrows(IllegalArgumentException.class, () -> service.updatePerson(null));
        }
    }

    @Nested
    class RemovePerson {
        @Test
        void shouldDeletePerson_whenExists() {
            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", "Addr", "City", "00000", "999", "email");
            when(personRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.of(person));

            service.removePerson("Steve", "Wonder");

            verify(personRepository).delete(person);
        }

        @Test
        void shouldNotDelete_whenNotExist() {
            when(personRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.empty());

            service.removePerson("Steve", "Wonder");

            verify(personRepository, never()).delete(any());
        }

        @Test
        void shouldThrowException_whenFirstNameNull() {
            assertThrows(IllegalArgumentException.class, () -> service.removePerson(null, "Wonder"));
        }

        @Test
        void shouldThrowException_whenLastNameNull() {
            assertThrows(IllegalArgumentException.class, () -> service.removePerson("Steve", null));
        }
    }

    // #endregion CRUD Operations

    // ----------------------------------------------------------------------------------
    // #region: Children by Address
    // ----------------------------------------------------------------------------------

    @Nested
    class ChildrenByAddress {

        @Test
        void getChildrenByAddress_shouldReturnEmpty_whenNoPersonAtAddress() {
            Mockito.when(personRepository.findByAddress("100 Street City"))
                    .thenReturn(List.of());

            List<ChildAlertDTO> result = service.getChildrenByAddress("100 Street City");

            assertTrue(result.isEmpty());
        }

        @Test
        void getChildrenByAddress_shouldReturnEmpty_whenNoChildren() {
            Person adult = ObjectFactoryTest.createPerson("John", "Doe", "100 Street City", "Miami", "00000", "123", "john@example.com");

            // Ce MedicalRecord doit marquer l'utilisateur comme MAJEUR
            MedicalRecord mr = ObjectFactoryTest.createMedicalRecord("John", "Doe", LocalDate.of(1980, 1, 1), List.of(), List.of());

            doReturn(List.of(adult))
                    .when(personRepository)
                    .findByAddress("100 Street City");

            doReturn(Optional.of(mr))
                    .when(medicalRecordRepository)
                    .findByFirstNameAndLastName("John", "Doe");

            List<ChildAlertDTO> result = service.getChildrenByAddress("100 Street City");

            assertTrue(result.isEmpty());
        }

        @Test
        void getChildrenByAddress_shouldReturnChildren_whenPresent() {
            Person child = ObjectFactoryTest.createPerson("Alice", "Doe", "100 Street City", "Miami", "00000", "123", "alice@example.com");
            Person parent = ObjectFactoryTest.createPerson("John", "Doe", "100 Street City", "Miami", "00000", "321", "john@example.com");

            MedicalRecord childMr = ObjectFactoryTest.createMedicalRecord("Alice", "Doe", LocalDate.now().minusYears(10), List.of(), List.of()); // enfant
            MedicalRecord parentMr = ObjectFactoryTest.createMedicalRecord("John", "Doe", LocalDate.of(1980, 1, 1), List.of(), List.of());

            Mockito.when(personRepository.findByAddress("100 Street City"))
                    .thenReturn(List.of(child, parent));

            Mockito.when(medicalRecordRepository.findByFirstNameAndLastName("Alice", "Doe"))
                    .thenReturn(Optional.of(childMr));
            Mockito.when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                    .thenReturn(Optional.of(parentMr));

            List<ChildAlertDTO> result = service.getChildrenByAddress("100 Street City");

            assertEquals(1, result.size());
            assertEquals("Alice", result.getFirst().firstName());
        }

        @Test
        void getChildrenByAddress_shouldThrowErrorSystemException_whenMedicalRecordMissing() {
            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", "100 Street City", "Miami", "00000", "999", "email@example.com");

            Mockito.when(personRepository.findByAddress("100 Street City"))
                    .thenReturn(List.of(person));

            Mockito.when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder"))
                    .thenReturn(Optional.empty());

            assertThrows(ErrorSystemException.class, () -> service.getChildrenByAddress("100 Street City"));
        }

        @Test
        void getChildrenByAddress_shouldThrowException_whenAddressIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.getChildrenByAddress(null));
        }
    }

    // ----------------------------------------------------------------------------------
    // #region: PhonesNumbersByFireStation
    // ----------------------------------------------------------------------------------

    @Nested
    class GetPhonesNumbersByFireStation {
        @Test
        void getPhoneNumbersByFireStation_shouldReturnPhones() {
            Integer stationNumber = 1;
            List<String> addresses = List.of("1 Rue de Paris");
            List<Person> persons = List.of(ObjectFactoryTest.createPerson("Steve", "Wonder", "100 Street City", "00000", "Miami", "999-999-666", "steve_wonder@gmail.com"));

            when(fireStationRepository.findAddressByNumberStation(stationNumber))
                    .thenReturn(addresses);
            when(personRepository.findByAddresses(addresses))
                    .thenReturn(persons);

            List<String> result = service.getPhoneNumbersByFireStation(stationNumber);

            assertEquals(List.of("999-999-666"), result);
        }

        @Test
        void getPhoneNumbersByFireStation_shouldReturnEmptyList_whenNoAddressesFound() {
            Integer stationNumber = 99;

            when(fireStationRepository.findAddressByNumberStation(stationNumber))
                    .thenReturn(Collections.emptyList());

            List<String> result = service.getPhoneNumbersByFireStation(stationNumber);

            assertTrue(result.isEmpty());
        }

        @Test
        void getPhoneNumbersByFireStation_shouldThrowException_whenStationNumberIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.getPhoneNumbersByFireStation(null));
        }

        @Test
        void getPhoneNumbersByFireStation_shouldThrowException_whenStationNumberIsNegative() {
            assertThrows(IllegalArgumentException.class, () -> service.getPhoneNumbersByFireStation(-1));
        }

        @Test
        void getPhoneNumbersByFireStation_shouldReturnEmptyList_whenNoPersonsAtAddresses() {
            Integer stationNumber = 1;
            List<String> addresses = List.of("1 Rue de Paris");

            when(fireStationRepository.findAddressByNumberStation(stationNumber))
                    .thenReturn(addresses);
            when(personRepository.findByAddresses(addresses))
                    .thenReturn(Collections.emptyList());

            List<String> result = service.getPhoneNumbersByFireStation(stationNumber);

            assertTrue(result.isEmpty());
        }
    }

    // ----------------------------------------------------------------------------------
    // #region: PersonnesAndFireStationByAddress
    // ----------------------------------------------------------------------------------

    @Nested
    class PersonnesAndFireStationByAddress {
        @Test
        void getPersonnesAndStationNumberByAddress_shouldReturnResponseDTO_whenDataExists() {
            String address = "100 Street City";

            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", address, "Miami", "00000", "999-999-666", "steve_wonder@gmail.com");
            MedicalRecord medicalRecord = ObjectFactoryTest.createMedicalRecord("Steve", "Wonder", LocalDate.of(1990, 1, 1), List.of(), List.of());
            FireStation fireStation = ObjectFactoryTest.createFireStation(address, 1);

            when(personRepository.findByAddress(address)).thenReturn(List.of(person));
            when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.of(medicalRecord));
            when(fireStationRepository.findByAddress(address)).thenReturn(Optional.of(fireStation));

            Optional<ResponseFireDTO> result = service.getPersonnesAndStationNumberByAddress(address);

            assertTrue(result.isPresent());
            ResponseFireDTO dto = result.get();

            // Vérification du numéro de station
            assertEquals(1, dto.stationNumber());

            // Vérification de la liste des persons (PersonsFireDTO)
            assertEquals(1, dto.persons().size());

            ResponseFireDTO.PersonsFireDTO personsFireDTO = dto.persons().getFirst();

            // Vérifie que le DTO contient bien les bonnes infos sur la personne
            assertEquals(person.getFirstName(), personsFireDTO.persons().firstName());
            assertEquals(person.getLastName(), personsFireDTO.persons().lastName());
            assertEquals(person.getAddressComplete(), personsFireDTO.persons().address());

            // Vérifie que les médicaments et allergies correspondent
            assertEquals(medicalRecord.getMedications(), personsFireDTO.medications());
            assertEquals(medicalRecord.getAllergies(), personsFireDTO.allergies());
        }

        @Test
        void getPersonnesAndStationNumberByAddress_shouldThrowNotFoundException_whenNoData() {
            String address = "Unknown Address";

            when(personRepository.findByAddress(address)).thenReturn(Collections.emptyList());
            when(fireStationRepository.findByAddress(address)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getPersonnesAndStationNumberByAddress(address));

            assertEquals("Aucune Données n'as été trouvé pour l'adresse: " + address, ex.getMessage());
        }

        @Test
        void getPersonnesAndStationNumberByAddress_shouldThrowErrorSystemException_whenMedicalRecordMissing() {
            String address = "100 Street City";

            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", address, "Miami", "00000", "999-999-666", "steve_wonder@gmail.com");

            when(personRepository.findByAddress(address)).thenReturn(List.of(person));
            when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.empty());

            assertThrows(ErrorSystemException.class, () -> service.getPersonnesAndStationNumberByAddress(address));
        }

        @Test
        void getPersonnesAndStationNumberByAddress_shouldThrowException_whenAddressIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.getPersonnesAndStationNumberByAddress(null));
        }

        @Test
        void getPersonnesAndStationNumberByAddress_shouldThrowException_whenAddressIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> service.getPersonnesAndStationNumberByAddress(""));
        }
    }

    // ----------------------------------------------------------------------------------
    // #region: PersonnesAndAddressByNumberFireStation
    // ----------------------------------------------------------------------------------

    @Nested
    class PersonnesAndAddressByNumberFireStation {
        @Test
        void getPersonnesAndAddressByNumberFireStation_shouldReturnFloodResponse_whenDataExists() {
            Integer stationNumber = 1;
            String address = "123 Main St";

            Person person = ObjectFactoryTest.createPerson("John", "Doe", address, "City", "75000", "0123456789", "john@example.com");
            MedicalRecord mr = ObjectFactoryTest.createMedicalRecord("John", "Doe", LocalDate.of(2000, 1, 1), List.of("med1"), List.of("all1"));

            when(fireStationRepository.findAddressByNumberStation(stationNumber)).thenReturn(List.of(address));
            when(personRepository.findByAddresses(List.of(address))).thenReturn(List.of(person));
            when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(Optional.of(mr));

            List<FloodResponseDTO> result = service.getPersonnesAndAddressByNumberFireStation(List.of(stationNumber));

            assertEquals(1, result.size());
            FloodResponseDTO dto = result.getFirst();
            assertEquals(address, dto.address());
            assertEquals(1, dto.personInfo().size());

            FloodResponseDTO.PersonInfoDTO info = dto.personInfo().getFirst();
            assertEquals("0123456789", info.numberPhone());
            assertEquals(mr.getAge(), info.age());

            List<String> expectedInfoList = List.of(
                    "John", "Doe",
                    "Médicaments: med1",
                    "Allergies: all1"
            );
            assertEquals(expectedInfoList, info.infoNameAndMedicationsAndAllergies());
        }

        @Test
        void getPersonnesAndAddressByNumberFireStation_shouldGroupCorrectly_whenMultipleStations() {
            List<Integer> stations = List.of(1, 2);
            String address1 = "1 Rue Alpha";
            String address2 = "2 Rue Beta";

            Person person1 = ObjectFactoryTest.createPerson("Alice", "Smith", address1, "City", "00000", "0101010101", "alice@email.com");
            Person person2 = ObjectFactoryTest.createPerson("Bob", "Jones", address2, "City", "00000", "0202020202", "bob@email.com");

            MedicalRecord mr1 = ObjectFactoryTest.createMedicalRecord("Alice", "Smith", LocalDate.of(2010, 5, 5), List.of("aspirin"), List.of());
            MedicalRecord mr2 = ObjectFactoryTest.createMedicalRecord("Bob", "Jones", LocalDate.of(1980, 3, 3), List.of(), List.of("pollen"));

            when(fireStationRepository.findAddressByNumberStation(1)).thenReturn(List.of(address1));
            when(fireStationRepository.findAddressByNumberStation(2)).thenReturn(List.of(address2));
            when(personRepository.findByAddresses(List.of(address1, address2))).thenReturn(List.of(person1, person2));
            when(medicalRecordRepository.findByFirstNameAndLastName("Alice", "Smith")).thenReturn(Optional.of(mr1));
            when(medicalRecordRepository.findByFirstNameAndLastName("Bob", "Jones")).thenReturn(Optional.of(mr2));

            List<FloodResponseDTO> result = service.getPersonnesAndAddressByNumberFireStation(stations);

            assertEquals(2, result.size());
        }

        @Test
        void getPersonnesAndAddressByNumberFireStation_shouldThrowException_whenStationNumberIsNull() {
            List<Integer> stationNumbers = new ArrayList<>();
            stationNumbers.add(1);
            stationNumbers.add(null);
            assertThrows(IllegalArgumentException.class, () -> service.getPersonnesAndAddressByNumberFireStation(stationNumbers));
        }

        @Test
        void getPersonnesAndAddressByNumberFireStation_shouldThrowException_whenStationListEmpty() {
            assertThrows(IllegalArgumentException.class, () -> service.getPersonnesAndAddressByNumberFireStation(List.of()));
        }

        @Test
        void getPersonnesAndAddressByNumberFireStation_shouldThrowNotFoundException_whenNoAddresses() {
            when(fireStationRepository.findAddressByNumberStation(1)).thenReturn(Collections.emptyList());

            NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getPersonnesAndAddressByNumberFireStation(List.of(1)));

            assertEquals("Aucune FireStations n'existe avec les numéros de station: [1]", ex.getMessage());
        }

        @Test
        void getPersonnesAndAddressByNumberFireStation_shouldThrowErrorSystemException_whenMedicalRecordMissing() {
            String address = "123 Main St";
            Person person = ObjectFactoryTest.createPerson("Jane", "Doe", address, "City", "75000", "0123456789", "jane@example.com");

            when(fireStationRepository.findAddressByNumberStation(1)).thenReturn(List.of(address));
            when(personRepository.findByAddresses(List.of(address))).thenReturn(List.of(person));
            when(medicalRecordRepository.findByFirstNameAndLastName("Jane", "Doe")).thenReturn(Optional.empty());

            assertThrows(ErrorSystemException.class, () -> service.getPersonnesAndAddressByNumberFireStation(List.of(1)));
        }
    }

    // ----------------------------------------------------------------------------------
    // #region: PersonByLastName
    // ----------------------------------------------------------------------------------

    @Nested
    class PersonsByLastName {
        @Test
        void getPersonsByLastName_shouldReturnList_whenPersonsFound() {
            // Given
            String lastName = "Wonder";
            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", "100 Street City", "Miami", "00000", "999-999-666", "steve_wonder@gmail.com");
            MedicalRecord mr = ObjectFactoryTest.createMedicalRecord("Steve", "Wonder", LocalDate.of(1980, 3, 3), List.of("med1"), List.of("pollen"));

            when(personRepository.findAllByLastName(lastName)).thenReturn(List.of(person));
            when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.of(mr));

            // When
            List<PersonInfosLastNameDTO> result = service.getPersonsByLastName(lastName);

            // Then
            assertEquals(1, result.size());
            PersonInfosLastNameDTO dto = result.getFirst();
            assertEquals("Steve", dto.firstName());
            assertEquals("Wonder", dto.lastName());
            assertEquals("100 Street City 00000 Miami", dto.address());
            assertEquals(45, dto.age());
            assertEquals("steve_wonder@gmail.com", dto.mail());
            assertEquals(List.of("med1"), dto.medications());
            assertEquals(List.of("pollen"), dto.allergies());
        }

        @Test
        void getPersonsByLastName_shouldThrowNotFound_whenNoPersonFound() {
            String lastName = "Unknown";

            when(personRepository.findAllByLastName(lastName)).thenReturn(List.of());

            assertThrows(NotFoundException.class, () -> service.getPersonsByLastName(lastName));
        }

        @Test
        void getPersonsByLastName_shouldThrowErrorSystem_whenMedicalRecordMissing() {
            String lastName = "Wonder";
            Person person = ObjectFactoryTest.createPerson("Steve", "Wonder", "100 Street City", "Miami", "00000", "999-999-666", "steve_wonder@gmail.com");

            when(personRepository.findAllByLastName(lastName)).thenReturn(List.of(person));
            when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.empty());

            assertThrows(ErrorSystemException.class, () -> service.getPersonsByLastName(lastName));
        }

        @Test
        void getPersonsByLastName_shouldThrowIllegalArgument_whenInputIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.getPersonsByLastName(null));
        }
    }

    // ----------------------------------------------------------------------------------
    // #region: MailByCity
    // ----------------------------------------------------------------------------------

    @Nested
    class MailByCity {
        @Test
        void getMailByCity_shouldReturnEmails_whenCityMatches() {
            // Given
            Person p1 = ObjectFactoryTest.createPerson("John", "Doe", "1 Rue A", "Paris", "75000", "0101010101", "john@example.com");
            Person p2 = ObjectFactoryTest.createPerson("Jane", "Doe", "2 Rue B", "Paris", "75000", "0202020202", "jane@example.com");
            Person p3 = ObjectFactoryTest.createPerson("Jack", "Smith", "3 Rue C", "Lyon", "69000", "0303030303", "jack@example.com");

            when(personRepository.getAll()).thenReturn(List.of(p1, p2, p3));

            // When
            List<String> emails = service.getMailByCity("Paris");

            // Then
            assertEquals(2, emails.size());
            assertTrue(emails.contains("john@example.com"));
            assertTrue(emails.contains("jane@example.com"));
        }

        @Test
        void getMailByCity_shouldIgnoreCase_whenMatchingCity() {
            Person p = ObjectFactoryTest.createPerson("John", "Doe", "1 Rue A", "PARIS", "75000", "0101010101", "john@example.com");

            when(personRepository.getAll()).thenReturn(List.of(p));

            List<String> emails = service.getMailByCity("paris");

            assertEquals(List.of("john@example.com"), emails);
        }

        @Test
        void getMailByCity_shouldThrowNotFoundException_whenNoneFound() {
            when(personRepository.getAll())
                    .thenReturn(List.of(ObjectFactoryTest.createPerson("Steve", "Wonder", "100 Street City", "00000", "Miami", "999-999-666", "steve_wonder@gmail.com")));

            assertThrows(NotFoundException.class, () -> service.getMailByCity("Miami"));
        }

        @Test
        void getMailByCity_shouldThrowIllegalArgumentException_whenCityIsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.getMailByCity(null));
        }

        @Test
        void getMailByCity_shouldReturnDistinctEmails() {
            Person p1 = ObjectFactoryTest.createPerson("John", "Doe", "1 Rue A", "Paris", "75000", "0101010101", "same@example.com");
            Person p2 = ObjectFactoryTest.createPerson("Jane", "Doe", "2 Rue B", "Paris", "75000", "0202020202", "same@example.com");

            when(personRepository.getAll()).thenReturn(List.of(p1, p2));

            List<String> emails = service.getMailByCity("Paris");

            assertEquals(1, emails.size());
            assertEquals("same@example.com", emails.getFirst());
        }
    }
}
