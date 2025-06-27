package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.repository.MedicalRecordRepository;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Classe de tests d'intégration pour le contrôleur MedicalRecordController.
 * <p>
 * Cette classe vérifie le comportement des endpoints liés aux enregistrements médicaux
 * (MedicalRecord) à travers des requêtes HTTP simulées avec MockMvc.
 * <p>
 * Les cas testés incluent
 * - La création, la mise à jour, et la suppression d'un enregistrement médical.
 * - Les cas d'erreur comme la tentative de création d'un enregistrement déjà existant,
 *   ou la modification/suppression d'un enregistrement inexistant.
 * - Les validations liées aux champs requis (notamment le prénom et le nom).
 * <p>
 * Les données utilisées sont rechargées depuis un fichier JSON de fixtures avant chaque test,
 * garantissant l'isolation des tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MedicalRecordControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private MedicalRecordRepository  medicalRecordRepository;

    @BeforeEach
    public void resetFixture() throws IOException {
        dataStorage.initializeDataFile();
        dataStorage.loadData();
    }

    // CAS D'USAGE NORMAL (SANS PROBLEME)
    /**
     * Teste la création d'un nouveau dossier médical via POST.
     * Vérifie que le dossier est bien créé avec les bonnes données.
     */
    @Test
    public void testPostMedicalRecord() throws Exception {
        // GIVEN un nouveau dossier médical au format JSON
        String newMedicalRecordJson = """
        {
            "firstName":"Michel",
            "lastName":"Garnier",
            "birthdate":"03/06/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg"],
            "allergies":["nillacilan"]
        }
        """;

        // WHEN on POST le dossier médical
        mockMvc.perform(post("/medicalrecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newMedicalRecordJson))

                // THEN on s'attend à une réponse créée avec les bons champs
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Michel"))
                .andExpect(jsonPath("$.lastName").value("Garnier"))
                .andExpect(jsonPath("$.birthdate").value("03/06/1984"))
                .andExpect(jsonPath("$.medications").value(containsInAnyOrder("aznol:350mg", "hydrapermazol:100mg")))
                .andExpect(jsonPath("$.allergies").value(containsInAnyOrder("nillacilan")));

        // AND le dossier doit être présent dans le repository
        Optional<MedicalRecord> medicalRecord = medicalRecordRepository.findByFirstNameAndLastName("Michel", "Garnier");
        assertTrue(medicalRecord.isPresent());
    }

    /**
     * Teste la mise à jour d'un dossier médical existant via PUT.
     * Vérifie que les données sont correctement mises à jour.
     */
    @Test
    public void testPutMedicalRecord() throws Exception {
        // Given
        String UpdateMedicalRecordJson = """
        {
            "firstName":"John",
            "lastName":"Boyd",
            "birthdate":"03/12/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg", "test:50mg"],
            "allergies":["nillacilan"]
        }
        """;

        // when
        mockMvc.perform(put("/medicalrecord")
        .contentType(MediaType.APPLICATION_JSON)
        .content(UpdateMedicalRecordJson))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthdate").value("03/12/1984"))
                .andExpect(jsonPath("$.medications[2]").value("test:50mg"));

        // And Then
        MedicalRecord medicalRecord = medicalRecordRepository.findByFirstNameAndLastName("John", "Boyd").orElseThrow();
        assertEquals(LocalDate.of(1984, 3, 12), medicalRecord.getBirthDate());
        assertEquals("test:50mg", medicalRecord.getMedications().get(2));
    }

    /**
     * Teste la suppression d'un dossier médical par prénom et nom.
     * Vérifie que le dossier est supprimé de la base.
     */
    @Test
    public void testDeleteMedicalRecord() throws Exception {
        // given
        String firstName = "John";
        String lastName = "Boyd";

        // When
        mockMvc.perform(delete("/medicalrecord")
        .param("firstName", firstName)
        .param("lastName", lastName))
                // Then
                .andExpect(status().isNoContent());

        // And Then
        Optional<MedicalRecord> medicalRecordDeleted = medicalRecordRepository.findByFirstNameAndLastName(firstName, lastName);
        assertFalse(medicalRecordDeleted.isPresent());
    }

    // CAS D'USAGE OU LE MEDICAL RECORD EST PRESENT OU PAS QUAND IL NE LE FAUT PAS

    /**
     * Teste la création d'un dossier médical déjà existant.
     * Vérifie que le serveur renvoie un conflit (409).
     */
    @Test
    public void testPostMedicalRecordButMedicalRecordAlreadyExist() throws Exception {
        // Given
        String expectedResult = "Medical record for this person already exists";
        String MedicalRecordExistingJson = """
        {
            "firstName":"John",
            "lastName":"Boyd",
            "birthdate":"03/12/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg", "test:50mg"],
            "allergies":["nillacilan"]
        }
        """;

        // When
        mockMvc.perform(post("/medicalrecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MedicalRecordExistingJson))
                // Then
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la mise à jour d'un dossier médical qui n'existe pas.
     * Vérifie que le serveur renvoie un 404.
     */
    @Test
    public void testPutMedicalRecordButMedicalRecordDoesntExist() throws Exception {
        // Given
        String expectedResult = "Medical record for this person does not exist";
        String MedicalRecordDoesntExistJson = """
        {
            "firstName":"Michel",
            "lastName":"Garnier",
            "birthdate":"03/06/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg"],
            "allergies":["nillacilan"]
        }
        """;

        // When
        mockMvc.perform(put("/medicalrecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MedicalRecordDoesntExistJson))
                // Then
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la suppression d'un dossier médical qui n'existe pas.
     * Vérifie que le serveur renvoie un 404.
     */
    @Test
    public void testDeleteMedicalRecordButMedicalRecordDoesntExist() throws Exception {
        // Given
        String firstName = "Michel";
        String lastName = "Garnier";
        String expectedResult = "";

        // When
        mockMvc.perform(delete("/medicalrecord")
                .param("firstName", firstName)
                .param("lastName", lastName))
                // Then
                .andExpect(status().isNoContent())
                .andExpect(content().string(containsString(expectedResult)));
    }

    // CAS D'USAGE OU LE FIRST OU LASTNAME = null

    /**
     * Teste la création d'un dossier médical avec un prénom null.
     * Vérifie que le serveur renvoie un 400 Bad Request avec message d'erreur.
     */
    @Test
    public void testPostMedicalRecordButMedicalRecordFirstNameIsNull() throws Exception {
        // Given
        String expectedResult = "firstName must not be null";
        String newMedicalRecordJson = """
        {
            "firstName": null,
            "lastName":"Garnier",
            "birthdate":"03/06/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg"],
            "allergies":["nillacilan"]
        }
        """;

        // When
        mockMvc.perform(post("/medicalrecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newMedicalRecordJson))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la mise à jour d'un dossier médical avec un prénom null.
     * Vérifie que le serveur renvoie un 400 Bad Request avec message d'erreur.
     */
    @Test
    public void testPutMedicalRecordButMedicalRecordLastNameIsNull() throws Exception {
        // Given
        String expectedResult = "lastName must not be null";
        String MedicalRecordDoesntExistJson = """
        {
            "firstName": "Michel",
            "lastName": null,
            "birthdate":"03/06/1984",
            "medications":["aznol:350mg","hydrapermazol:100mg"],
            "allergies":["nillacilan"]
        }
        """;

        // When
        mockMvc.perform(put("/medicalrecord")
        .contentType(MediaType.APPLICATION_JSON)
        .content(MedicalRecordDoesntExistJson))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedResult)));
    }

    // CAS D'USAGE OU ON A NULL DANS LE BODY

    /**
     * Teste la création d'un dossier médical avec un corps de requête null.
     * Vérifie que le serveur renvoie un 400 Bad Request.
     */
    @Test
    public void testPostMedicalRecordButIsNull() throws Exception {
        // Given
        String content = "null";
        String expectedResult = "Request body is invalid or missing";

        // When
        mockMvc.perform(post("/medicalrecord")
        .contentType(MediaType.APPLICATION_JSON)
        .content(content))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la mise à jour d'un dossier médical avec un corps de requête null.
     * Vérifie que le serveur renvoie un 400 Bad Request.
     */
    @Test
    public void testPutMedicalRecordButIsNull() throws Exception {
        // Given
        String content = "null";
        String expectedResult = "Request body is invalid or missing";

        // When
        mockMvc.perform(put("/medicalrecord")
        .contentType(MediaType.APPLICATION_JSON)
        .content(content))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedResult)));
    }
}
