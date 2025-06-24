package com.safetynet.AppSafetyNet;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.repository.Impl.MedicalRecordRepositoryImpl;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import com.safetynet.AppSafetyNet.utils.ObjectFactoryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordRepositoryImplTest {
    @Mock
    private DataStorage dataStorage;

    @InjectMocks
    private MedicalRecordRepositoryImpl repository;


    @Nested
    class FindByFirstNameAndLastName {
        @Test
        void shouldReturnRecord_whenExists() {
            MedicalRecord mr = ObjectFactoryTest.createMedicalRecord("Alice", "Blue", LocalDate.of(1995, 5, 10), List.of("med1"), List.of());
            when(dataStorage.getMedicalRecords()).thenReturn(List.of(mr));

            Optional<MedicalRecord> result = repository.findByFirstNameAndLastName("Alice", "Blue");

            assertThat(result).isPresent().contains(mr);
        }

        @Test
        void shouldReturnEmpty_whenNotFound() {
            when(dataStorage.getMedicalRecords()).thenReturn(new ArrayList<>());

            Optional<MedicalRecord> result = repository.findByFirstNameAndLastName("Unknown", "User");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldThrowException_whenFirstNameIsNull() {
            assertThatThrownBy(() -> repository.findByFirstNameAndLastName(null, "Smith"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowException_whenLastNameIsNull() {
            assertThatThrownBy(() -> repository.findByFirstNameAndLastName("John", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class SaveOrUpdateMedicalRecord {

        @Test
        void shouldSaveRecord_whenNotExists() {
            List<MedicalRecord> medicalRecords = new ArrayList<>();
            when(dataStorage.getMedicalRecords()).thenReturn(medicalRecords);

            MedicalRecord mr = ObjectFactoryTest.createMedicalRecord("Bob", "Green", LocalDate.of(1980, 1, 1), List.of(), List.of());

            repository.saveOrUpdateMedicalRecord(mr);

            assertThat(medicalRecords).containsExactly(mr);
        }

        @Test
        void shouldUpdateRecord_whenAlreadyExists() {
            MedicalRecord old = ObjectFactoryTest.createMedicalRecord("Jane", "Doe", LocalDate.of(1970, 1, 1), List.of("old"), List.of());
            List<MedicalRecord> medicalRecords = new ArrayList<>(List.of(old));
            when(dataStorage.getMedicalRecords()).thenReturn(medicalRecords);

            MedicalRecord updated = ObjectFactoryTest.createMedicalRecord("Jane", "Doe", LocalDate.of(2000, 1, 1), List.of("new"), List.of());

            repository.saveOrUpdateMedicalRecord(updated);

            assertThat(medicalRecords).containsExactly(updated);
        }

        @Test
        void shouldThrowException_whenNull() {
            assertThatThrownBy(() -> repository.saveOrUpdateMedicalRecord(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class DeleteMedicalRecord {

        void shouldDeleteRecord_whenExists() {
            MedicalRecord mr = ObjectFactoryTest.createMedicalRecord("Chris", "Red", LocalDate.of(1985, 3, 15), List.of(), List.of());
            List<MedicalRecord> medicalRecords = new ArrayList<>(List.of(mr));
            when(dataStorage.getMedicalRecords()).thenReturn(medicalRecords);

            repository.deleteMedicalRecord(mr);

            assertThat(medicalRecords).isEmpty();
            verify(dataStorage).saveData();
        }

        @Test
        void shouldThrowException_whenNull() {
            assertThatThrownBy(() -> repository.deleteMedicalRecord(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class GetMedicalRecordByPerson {

        @Test
        void shouldReturnRecord_whenExists() {
            MedicalRecord mr = ObjectFactoryTest.createMedicalRecord("Anna", "White", LocalDate.of(1990, 6, 25), List.of(), List.of());
            when(dataStorage.getMedicalRecords()).thenReturn(List.of(mr));

            MedicalRecord result = repository.getMedicalRecordByPerson("Anna", "White");

            assertThat(result).isEqualTo(mr);
        }

        @Test
        void shouldThrow_whenNotFound() {
            assertThatThrownBy(() -> repository.getMedicalRecordByPerson("Ghost", "User"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("does not exist");
        }

        @Test
        void shouldThrow_whenFirstNameIsNull() {
            assertThatThrownBy(() -> repository.getMedicalRecordByPerson(null, "Doe"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrow_whenLastNameIsNull() {
            assertThatThrownBy(() -> repository.getMedicalRecordByPerson("John", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
