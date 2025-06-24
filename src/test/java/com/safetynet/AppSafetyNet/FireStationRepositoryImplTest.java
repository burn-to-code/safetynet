package com.safetynet.AppSafetyNet;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.repository.Impl.FireStationRepositoryImpl;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import com.safetynet.AppSafetyNet.utils.ObjectFactoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FireStationRepositoryImplTest {
    @Mock
    private DataStorage dataStorage;

    private FireStationRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new FireStationRepositoryImpl(dataStorage);
    }

    @Test
    void getAll_shouldReturnAllFireStations() {
        List<FireStation> stations = List.of(
                ObjectFactoryTest.createFireStation("1 rue Bleue", 1),
                ObjectFactoryTest.createFireStation("2 rue Verte", 2)
        );
        when(dataStorage.getFireStations()).thenReturn(stations);

        List<FireStation> result = repository.getAll();

        assertThat(result).isEqualTo(stations);
    }

    @Nested
    class FindByAddress {
        @Test
        void findByAddress_shouldReturnFireStation_whenExists() {
            FireStation fs = ObjectFactoryTest.createFireStation("1 rue Bleue", 1);
            when(dataStorage.getFireStations()).thenReturn(List.of(fs));

            Optional<FireStation> result = repository.findByAddress("1 rue Bleue");

            assertThat(result).contains(fs);
        }

        @Test
        void findByAddress_shouldReturnEmpty_whenNotFound() {
            when(dataStorage.getFireStations()).thenReturn(List.of());

            Optional<FireStation> result = repository.findByAddress("inconnue");

            assertThat(result).isEmpty();
        }

        @Test
        void findByAddress_shouldThrowException_whenAddressIsNull() {
            assertThatThrownBy(() -> repository.findByAddress(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be null");
        }
    }

    @Nested
    class SaveFireStation {
        @Test
        void saveFireStation_shouldReplaceExisting_andCallSaveData() {
            FireStation fs1 = ObjectFactoryTest.createFireStation("1 rue Bleue", 1);
            FireStation fs2 = ObjectFactoryTest.createFireStation("1 rue Bleue", 2); // mise à jour

            List<FireStation> fireStations = new ArrayList<>(List.of(fs1));
            when(dataStorage.getFireStations()).thenReturn(fireStations);

            repository.saveFireStation(fs2);

            assertThat(fireStations).containsExactly(fs2);
            verify(dataStorage).saveData();
        }

        @Test
        void saveFireStation_shouldThrow_whenNull() {
            assertThatThrownBy(() -> repository.saveFireStation(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be null");
        }

    }

    @Nested
    class DeleteFireStation {
        @Test
        void deleteFireStation_shouldRemoveStation_andCallSaveData() {
            FireStation fs = ObjectFactoryTest.createFireStation("1 rue Bleue", 1);
            List<FireStation> list = new ArrayList<>(List.of(fs));
            when(dataStorage.getFireStations()).thenReturn(list);

            repository.deleteFireStation(fs);

            assertThat(list).doesNotContain(fs);
            verify(dataStorage).saveData();
        }

        @Test
        void deleteFireStation_shouldThrow_whenNull() {
            assertThatThrownBy(() -> repository.deleteFireStation(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be null");
        }
    }

    @Nested
    class FindAddressByNumberStation {
        @Test
        void findAddressByNumberStation_shouldReturnAddresses() {
            List<FireStation> stations = List.of(
                    ObjectFactoryTest.createFireStation("1 rue Bleue", 1),
                    ObjectFactoryTest.createFireStation("2 rue Verte", 2),
                    ObjectFactoryTest.createFireStation("3 rue Jaune", 1)
            );
            when(dataStorage.getFireStations()).thenReturn(stations);

            List<String> result = repository.findAddressByNumberStation(1);

            assertThat(result).containsExactlyInAnyOrder("1 rue Bleue", "3 rue Jaune");
        }

        @Test
        void findAddressByNumberStation_shouldReturnEmpty_whenNoMatch() {
            when(dataStorage.getFireStations()).thenReturn(List.of());

            List<String> result = repository.findAddressByNumberStation(99);

            assertThat(result).isEmpty();
        }

        @Test
        void findAddressByNumberStation_shouldThrow_whenNull() {
            assertThatThrownBy(() -> repository.findAddressByNumberStation(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be null");
        }
    }

}

