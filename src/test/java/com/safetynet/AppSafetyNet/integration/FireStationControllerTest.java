package com.safetynet.AppSafetyNet.integration;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@TestPropertySource(properties = {
        "application.data-file-path=src/test/resources/fixtures/test-data.json"
})
@AutoConfigureMockMvc
public class FireStationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private FireStationRepository  fireStationRepository;

    @BeforeEach
    public void resetFixture() throws IOException {
        Files.copy(
                Path.of("src/test/resources/fixtures/test-data.json.orig"),
                Path.of("src/test/resources/fixtures/test-data.json"),
                StandardCopyOption.REPLACE_EXISTING
        );
        dataStorage.loadData();
    }

    @AfterAll
    public static void cleanFixture() throws IOException {
        Files.copy(
                Path.of("src/test/resources/fixtures/test-data.json.orig"),
                Path.of("src/test/resources/fixtures/test-data.json"),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    // TESTER LES CAS D'USAGES CORRECTS

    @Test
    public void testGetFireStationByNumberStation() throws Exception {
        mockMvc.perform(get("/firestation")
                        .param("stationNumber", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persons.length()").value(5))
                .andExpect(jsonPath("$.adults").value(3))
                .andExpect(jsonPath("$.children").value(2))
                .andExpect(jsonPath("$.persons[0].first_name").value("John"))
                .andExpect(jsonPath("$.persons[0].last_name").value("Boyd"))
                .andExpect(jsonPath("$.persons[0].address").value("1509 Culver St 97451 Culver"))
                .andExpect(jsonPath("$.persons[0].phone").value("841-874-6512"));
    }

    @Test
    public void testPostFireStation() throws Exception {
        String newFireStationJson = """
        {
            "address":"27 Boulevard St",
            "station":"3"
        }
        """;
        mockMvc.perform(post("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newFireStationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("27 Boulevard St"))
                .andExpect(jsonPath("$.station").value("3"));

        Optional<FireStation> newFireStation = fireStationRepository.findByAddress("27 Boulevard St");
        assertTrue(newFireStation.isPresent());
    }

    @Test
    public void testPutFireStation() throws Exception {
        // Vérifier Avant le put que la fireStation est différente du put
        FireStation fireStationActually = fireStationRepository.findByAddress("1509 Culver St").orElseThrow();
        assertEquals("1509 Culver St", fireStationActually.getAddress());
        assertEquals("3", fireStationActually.getStation());


        String updateFireStationJson = """
        {
            "address":"1509 Culver St",
            "station":"8"
        }
        """;
        mockMvc.perform(put("/firestation")
        .contentType(MediaType.APPLICATION_JSON)
        .content(updateFireStationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("1509 Culver St"))
                .andExpect(jsonPath("$.station").value("8"));

        Optional<FireStation> updatedStation = fireStationRepository.findByAddress("1509 Culver St");
        assertEquals("8", updatedStation.orElseThrow().getStation());
    }

    @Test
    public void testDeleteFireStation() throws Exception {
        Optional<FireStation> fireStation =  fireStationRepository.findByAddress("1509 Culver St");
        assertTrue(fireStation.isPresent());

        mockMvc.perform(delete("/firestation")
                .param("address", "1509 Culver St"))
                .andExpect(status().isNoContent());

        Optional<FireStation> fireStationDeleted =  fireStationRepository.findByAddress("1509 Culver St");
        assertFalse(fireStationDeleted.isPresent());
    }

    // TESTER LES CAS D'USAGES EXISTE OU N'EXISTE PAS QUAND IL NE LE DEVRAI PAS

    @Test
    public void testGetFireStationByNumberStationButNumberStationDoesntExist() throws Exception {
        mockMvc.perform(get("/firestation")
                        .param("stationNumber", "10"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Aucune adresse trouvée pour la caserne numéro 10 ce numéro de station ne doit pas exister"));
    }


    @Test
    public void testPostFireStationButFireStationAlreadyExist() throws Exception {
        String newFireStationJson = """
        {
            "address":"1509 Culver St",
            "station":"5"
        }
        """;
        mockMvc.perform(post("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newFireStationJson))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("FireStation already exists")));
    }

    @Test
    public void testPutFireStationButFireStationDoesntExist() throws Exception {
        String newFireStationJson = """
        {
            "address":"27 Boulevard St",
            "station":"5"
        }
        """;
        mockMvc.perform(put("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newFireStationJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("FireStation does not exist")));
    }

    @Test
    public void testDeleteFireStationButFireStationDoesntExist() throws Exception {
        mockMvc.perform(delete("/firestation")
        .param("address", "27 Boulevard St"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("FireStation does not exist")));
    }

    // TESTER QUAND ON RECOIS UNE FIRESTATION NULL

    @Test
    public void testPutFireStationButFireStationIsNull() throws Exception {
        mockMvc.perform(put("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Request body is invalid or missing")));
    }

    @Test
    public void testPostFireStationButFireStationIsNull() throws Exception {
        mockMvc.perform(post("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Request body is invalid or missing")));
    }


}
