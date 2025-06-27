package com.safetynet.AppSafetyNet.service;

import com.safetynet.AppSafetyNet.exception.ConflictException;
import com.safetynet.AppSafetyNet.exception.NotFoundException;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.service.Impl.MedicalRecordServiceImpl;
import com.safetynet.AppSafetyNet.utils.ObjectFactoryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordServiceTest {
    @InjectMocks
    private MedicalRecordServiceImpl service;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Nested
    class PutMedicalRecord {
        @Test
        void saveMedicalRecord_shouldSave_whenNewRecord() {
            MedicalRecord medicalRecord = ObjectFactoryTest.createMedicalRecord("John", "Doe", LocalDate.of(1990, 1, 1), List.of(), List.of());

            when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(Optional.empty());

            service.saveMedicalRecord(medicalRecord);

            verify(medicalRecordRepository).saveOrUpdateMedicalRecord(medicalRecord);
        }

        @Test
        void saveMedicalRecord_shouldThrowConflictException_whenRecordExists() {
            MedicalRecord medicalRecord = ObjectFactoryTest.createMedicalRecord("John", "Doe", LocalDate.of(1990, 1, 1), List.of(), List.of());

            when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                    .thenReturn(Optional.of(medicalRecord));

            ConflictException exception = assertThrows(ConflictException.class, () -> service.saveMedicalRecord(medicalRecord));

            assertEquals("Medical record for this person already exists", exception.getMessage());

            verify(medicalRecordRepository, never()).saveOrUpdateMedicalRecord(any());
        }
    }

    @Nested
    class UpdateMedicalRecordTests {

        @Test
        void updateMedicalRecord_shouldUpdate_whenRecordExists() {
            MedicalRecord existingRecord = ObjectFactoryTest.createMedicalRecord("Steve", "Wonder", LocalDate.of(1990, 1, 1), List.of("med1"), List.of("allergy1"));
            MedicalRecord updatedRecord = ObjectFactoryTest.createMedicalRecord("Steve", "Wonder", LocalDate.of(1991, 2, 2), List.of("med2"), List.of("allergy2"));

            when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.of(existingRecord));

            service.updateMedicalRecord(updatedRecord);

            verify(medicalRecordRepository).saveOrUpdateMedicalRecord(existingRecord);
            assertEquals(updatedRecord.getBirthDate(), existingRecord.getBirthDate());
            assertEquals(updatedRecord.getMedications(), existingRecord.getMedications());
            assertEquals(updatedRecord.getAllergies(), existingRecord.getAllergies());
        }

        @Test
        void updateMedicalRecord_shouldThrowNotFoundException_whenRecordDoesNotExist() {
            MedicalRecord updatedRecord = ObjectFactoryTest.createMedicalRecord("Steve", "Wonder", LocalDate.of(1991, 2, 2), List.of("med2"), List.of("allergy2"));

            when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> service.updateMedicalRecord(updatedRecord));

            assertEquals("Medical record for this person does not exist", exception.getMessage());
            verify(medicalRecordRepository, never()).saveOrUpdateMedicalRecord(any());
        }
    }

    @Nested
    class DeleteMedicalRecordTests {

        @Test
        void deleteMedicalRecord_shouldDelete_whenRecordExists() {
            MedicalRecord existingRecord = ObjectFactoryTest.createMedicalRecord("Steve", "Wonder", LocalDate.of(1990, 1, 1), List.of(), List.of());

            when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.of(existingRecord));

            service.deleteMedicalRecord("Steve", "Wonder");

            verify(medicalRecordRepository).deleteMedicalRecord(existingRecord);
        }

        @Test
        void deleteMedicalRecord_shouldDoNothing_whenRecordDoesNotExist() {
            when(medicalRecordRepository.findByFirstNameAndLastName("Steve", "Wonder")).thenReturn(Optional.empty());

            service.deleteMedicalRecord("Steve", "Wonder");

            verify(medicalRecordRepository, never()).deleteMedicalRecord(any());
        }
    }

    @Nested
    class NullChecks {

        @Test
        void saveMedicalRecord_shouldThrowException_whenNullInput() {
            assertThrows(IllegalArgumentException.class, () -> service.saveMedicalRecord(null));
        }

        @Test
        void updateMedicalRecord_shouldThrowException_whenNullInput() {
            assertThrows(IllegalArgumentException.class, () -> service.updateMedicalRecord(null));
        }

        @Test
        void deleteMedicalRecord_shouldThrowException_whenNullInput() {
            assertThrows(IllegalArgumentException.class, () -> service.deleteMedicalRecord(null, null));
        }
    }
}
