package com.safetynet.AppSafetyNet.integration;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = {
        "application.data-file-path=src/test/resources/fixtures/test-data.json"
})
@AutoConfigureMockMvc
public class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private MedicalRecordRepository  medicalRecordRepository;

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

    // CAS D'USAGE NORMAL (SANS PROBLEME)

    @Test
    public void testPostMedicalRecord() throws Exception {
        String newMedicalRecordJson = """
        {
            "firstName":"Michel",
            "lastName":"Garnier",
            "birthdate":"03/06/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg"],
            "allergies":["nillacilan"]
        }
        """;
        mockMvc.perform(post("/medicalrecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newMedicalRecordJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Michel"))
                .andExpect(jsonPath("$.lastName").value("Garnier"))
                .andExpect(jsonPath("$.birthdate").value("03/06/1984"))
                .andExpect(jsonPath("$.medications[0]").value("aznol:350mg"))
                .andExpect(jsonPath("$.medications[1]").value("hydrapermazol:100mg"))
                .andExpect(jsonPath("$.allergies[0]").value("nillacilan"));

        Optional<MedicalRecord> medicalRecord = medicalRecordRepository.findByFirstNameAndLastName("Michel", "Garnier");
        assertTrue(medicalRecord.isPresent());
    }

    @Test
    public void testPutMedicalRecord() throws Exception {
        String UpdateMedicalRecordJson = """
        {
            "firstName":"John",
            "lastName":"Boyd",
            "birthdate":"03/12/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg", "test:50mg"],
            "allergies":["nillacilan"]
        }
        """;
        mockMvc.perform(put("/medicalrecord")
        .contentType(MediaType.APPLICATION_JSON)
        .content(UpdateMedicalRecordJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthdate").value("03/12/1984"))
                .andExpect(jsonPath("$.medications[2]").value("test:50mg"));

        MedicalRecord medicalRecord = medicalRecordRepository.findByFirstNameAndLastName("John", "Boyd").orElseThrow();
        assertEquals(LocalDate.of(1984, 3, 12), medicalRecord.getBirthDate());
        assertEquals("test:50mg", medicalRecord.getMedications().get(2));
    }

    @Test
    public void testDeleteMedicalRecord() throws Exception {
        mockMvc.perform(delete("/medicalrecord")
        .param("firstName", "John")
        .param("lastName", "Boyd"))
                .andExpect(status().isNoContent());

        Optional<MedicalRecord> medicalRecordDeleted = medicalRecordRepository.findByFirstNameAndLastName("John", "Garnier");
        assertFalse(medicalRecordDeleted.isPresent());
    }

    // CAS D'USAGE OU LE MEDICAL RECORD EST PRESENT OU PAS QUAND IL NE LE FAUT PAS

    @Test
    public void testPostMedicalRecordButMedicalRecordAlreadyExist() throws Exception {
        String MedicalRecordExistingJson = """
        {
            "firstName":"John",
            "lastName":"Boyd",
            "birthdate":"03/12/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg", "test:50mg"],
            "allergies":["nillacilan"]
        }
        """;
        mockMvc.perform(post("/medicalrecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MedicalRecordExistingJson))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Medical record for this person already exists")));
    }

    @Test
    public void testPutMedicalRecordButMedicalRecordDoesntExist() throws Exception {
        String MedicalRecordDoesntExistJson = """
        {
            "firstName":"Michel",
            "lastName":"Garnier",
            "birthdate":"03/06/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg"],
            "allergies":["nillacilan"]
        }
        """;
        mockMvc.perform(put("/medicalrecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MedicalRecordDoesntExistJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Medical record for this person does not exist")));
    }

    @Test
    public void testDeleteMedicalRecordButMedicalRecordDoesntExist() throws Exception {
        mockMvc.perform(delete("/medicalrecord")
                .param("firstName", "Michel")
                .param("lastName", "Garnier"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Medical record for this person does not exist")));
    }

    // CAS D'USAGE OU LE FIRST OU LASTNAME = null

    @Test
    public void testPostMedicalRecordButMedicalRecordFirstNameIsNull() throws Exception {
        String newMedicalRecordJson = """
        {
            "firstName": null,
            "lastName":"Garnier",
            "birthdate":"03/06/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg"],
            "allergies":["nillacilan"]
        }
        """;
        mockMvc.perform(post("/medicalrecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newMedicalRecordJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("firstName must not be null")));
    }

    @Test
    public void testPutMedicalRecordButMedicalRecordFirstNameIsNull() throws Exception {
        String MedicalRecordDoesntExistJson = """
        {
            "firstName": null,
            "lastName":"Garnier",
            "birthdate":"03/06/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg"],
            "allergies":["nillacilan"]
        }
        """;
        mockMvc.perform(put("/medicalrecord")
        .contentType(MediaType.APPLICATION_JSON)
        .content(MedicalRecordDoesntExistJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("firstName must not be null")));
    }

    // CAS D'USAGE OU ON A NULL DANS LE BODY

    @Test
    public void testPostMedicalRecordButIsNull() throws Exception {
        mockMvc.perform(post("/medicalrecord")
        .contentType(MediaType.APPLICATION_JSON)
        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Request body is invalid or missing")));
    }

    @Test
    public void testPutMedicalRecordButIsNull() throws Exception {
        mockMvc.perform(put("/medicalrecord")
        .contentType(MediaType.APPLICATION_JSON)
        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Request body is invalid or missing")));
    }
}
