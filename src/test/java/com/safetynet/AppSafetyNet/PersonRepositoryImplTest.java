package com.safetynet.AppSafetyNet;

import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.repository.Impl.PersonRepositoryImpl;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import com.safetynet.AppSafetyNet.utils.ObjectFactoryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonRepositoryImplTest {
    @Mock
    private DataStorage dataStorageService;

    @InjectMocks
    private PersonRepositoryImpl personRepository;

    @Test
    void getAll_shouldReturnAllPersonsFromDataStorage() {
        // Arrange
        List<Person> expectedPersons = List.of(
                ObjectFactoryTest.createPerson("John", "Doe", "123 Street", "1234567890", "City", "Zip", "john@example.com"),
                ObjectFactoryTest.createPerson("Jane", "Smith", "456 Avenue", "0987654321", "City", "Zip", "jane@example.com")
        );
        when(dataStorageService.getPersons()).thenReturn(expectedPersons);

        // Act
        List<Person> actualPersons = personRepository.getAll();

        // Assert
        assertThat(actualPersons).isEqualTo(expectedPersons);
        verify(dataStorageService).getPersons();
    }

    @Nested
    class FindByFirstNameAndLastName {

        @Test
        void shouldReturnPerson_whenExists() {
            // Arrange
            Person person = ObjectFactoryTest.createPerson("John", "Doe", "123 Street", "1234567890", "City", "Zip", "john@example.com");
            when(dataStorageService.getPersons()).thenReturn(List.of(person));

            // Act
            Optional<Person> result = personRepository.findByFirstNameAndLastName("John", "Doe");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(person);
        }

        @Test
        void shouldReturnEmpty_whenPersonDoesNotExist() {
            // Arrange
            when(dataStorageService.getPersons()).thenReturn(List.of());

            // Act
            Optional<Person> result = personRepository.findByFirstNameAndLastName("John", "Doe");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void shouldThrowException_whenFirstNameIsNull() {
            // Assert
            assertThatThrownBy(() -> personRepository.findByFirstNameAndLastName(null, "Doe"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("First name must not be null");
        }

        @Test
        void shouldThrowException_whenLastNameIsNull() {
            // Assert
            assertThatThrownBy(() -> personRepository.findByFirstNameAndLastName("John", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Last name must not be null");
        }
    }

    @Nested
    class FindAllByLastName {

        @Test
        void shouldReturnMatchingPersons_whenLastNameMatches() {
            // Arrange
            Person person1 = ObjectFactoryTest.createPerson("John", "Doe", "123 Street", "1234567890", "City", "Zip", "john@example.com");
            Person person2 = ObjectFactoryTest.createPerson("Jane", "Doe", "124 Street", "0987654321", "City", "Zip", "jane@example.com");
            Person person3 = ObjectFactoryTest.createPerson("Alice", "Smith", "125 Street", "1111111111", "City", "Zip", "alice@example.com");

            when(dataStorageService.getPersons()).thenReturn(List.of(person1, person2, person3));

            // Act
            List<Person> result = personRepository.findAllByLastName("doe");

            // Assert
            assertThat(result)
                    .hasSize(2)
                    .containsExactlyInAnyOrder(person1, person2);
        }

        @Test
        void shouldReturnEmptyList_whenNoLastNameMatches() {
            // Arrange
            Person person = ObjectFactoryTest.createPerson("Alice", "Smith", "125 Street", "1111111111", "City", "Zip", "alice@example.com");
            when(dataStorageService.getPersons()).thenReturn(List.of(person));

            // Act
            List<Person> result = personRepository.findAllByLastName("Doe");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void shouldThrowException_whenLastNameIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> personRepository.findAllByLastName(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Last name must not be null");
        }

    }

    @Nested
    class Save {

        @Test
        void shouldUpdatePerson_whenPersonWithSameIdExists() {
            // Arrange
            Person existing = ObjectFactoryTest.createPerson("John", "Doe", "Old Address", "000", "City", "Zip", "old@mail.com");
            Person updated = ObjectFactoryTest.createPerson("John", "Doe", "New Address", "123", "City", "Zip", "new@mail.com");

            List<Person> persons = new ArrayList<>(List.of(existing));
            when(dataStorageService.getPersons()).thenReturn(persons);

            // Act
            personRepository.save(updated);

            // Assert
            assertThat(persons)
                    .hasSize(1)
                    .containsExactly(updated); // old one should be removed, new one added
            verify(dataStorageService).saveData();
        }

        @Test
        void shouldAddPerson_whenNotPresent() {
            // Arrange
            Person newPerson = ObjectFactoryTest.createPerson("Alice", "Smith", "Somewhere", "456", "City", "Zip", "alice@mail.com");
            List<Person> persons = new ArrayList<>();

            when(dataStorageService.getPersons()).thenReturn(persons);

            // Act
            personRepository.save(newPerson);

            // Assert
            assertThat(persons)
                    .hasSize(1)
                    .containsExactly(newPerson);
            verify(dataStorageService).saveData();
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldDeletePerson_whenPersonExists() {
            // Arrange
            Person person = ObjectFactoryTest.createPerson("Jane", "Doe", "Address", "111", "City", "Zip", "jane@mail.com");
            List<Person> persons = new ArrayList<>(List.of(person));
            when(dataStorageService.getPersons()).thenReturn(persons);

            // Act
            personRepository.delete(person);

            // Assert
            assertThat(persons).doesNotContain(person);
            verify(dataStorageService).saveData();
        }

        @Test
        void shouldDoNothing_whenPersonNotExists() {
            // Arrange
            Person person1 = ObjectFactoryTest.createPerson("Jane", "Doe", "Address", "111", "City", "Zip", "jane@mail.com");
            Person person2 = ObjectFactoryTest.createPerson("Not", "Exist", "Address", "000", "City", "Zip", "ghost@mail.com");
            List<Person> persons = new ArrayList<>(List.of(person1));
            when(dataStorageService.getPersons()).thenReturn(persons);

            // Act
            personRepository.delete(person2);

            // Assert
            assertThat(persons).containsExactly(person1); // list unchanged
            verify(dataStorageService).saveData(); // always called
        }
    }

    @Nested
    class FindByAddresses {

        @Test
        void shouldReturnPersons_whenAddressMatchesIgnoringCase() {
            // Arrange
            Person p1 = ObjectFactoryTest.createPerson("Alice", "Doe", "123 Main St", "123", "City", "Zip", "a@a.com");
            Person p2 = ObjectFactoryTest.createPerson("Bob", "Doe", "456 Oak St", "456", "City", "Zip", "b@b.com");
            List<Person> persons = List.of(p1, p2);
            when(dataStorageService.getPersons()).thenReturn(persons);

            List<String> addresses = List.of("123 MAIN st", "789 Pine St");

            // Act
            List<Person> result = personRepository.findByAddresses(addresses);

            // Assert
            assertThat(result).containsExactly(p1);
        }

        @Test
        void shouldReturnEmptyList_whenNoAddressMatches() {
            // Arrange
            Person p1 = ObjectFactoryTest.createPerson("Alice", "Doe", "123 Main St", "123", "City", "Zip", "a@a.com");
            List<Person> persons = List.of(p1);
            when(dataStorageService.getPersons()).thenReturn(persons);

            List<String> addresses = List.of("000 Unknown St");

            // Act
            List<Person> result = personRepository.findByAddresses(addresses);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnMultiplePersons_whenMultipleMatch() {
            // Arrange
            Person p1 = ObjectFactoryTest.createPerson("Alice", "Doe", "123 Main St", "123", "City", "Zip", "a@a.com");
            Person p2 = ObjectFactoryTest.createPerson("Bob", "Doe", "123 Main St", "456", "City", "Zip", "b@b.com");
            Person p3 = ObjectFactoryTest.createPerson("Charlie", "Doe", "789 Pine St", "789", "City", "Zip", "c@c.com");
            List<Person> persons = List.of(p1, p2, p3);
            when(dataStorageService.getPersons()).thenReturn(persons);

            List<String> addresses = List.of("123 main st", "789 PINE ST");

            // Act
            List<Person> result = personRepository.findByAddresses(addresses);

            // Assert
            assertThat(result).containsExactlyInAnyOrder(p1, p2, p3);
        }
    }

}
