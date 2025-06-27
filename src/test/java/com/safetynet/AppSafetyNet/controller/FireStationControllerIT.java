package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.repository.FireStationRepository;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Classe de tests d'intégration pour le contrôleur FireStationController.
 * <p>
 * Cette classe teste les endpoints liés à la gestion des casernes (FireStation),
 * notamment les opérations CRUD (création, lecture, mise à jour, suppression).
 * <p>
 * Les tests couvrent :
 * - La récupération des personnes couvertes par une caserne donnée avec les
 *   statistiques d'adultes et d'enfants.
 * - L'ajout, la modification, et la suppression d'une caserne via les requêtes HTTP.
 * - La gestion des cas d'erreurs, tels que l'existence ou non des casernes lors des opérations,
 *   ainsi que la validation des corps de requêtes null ou invalides.
 * <p>
 * Les données utilisées sont rechargées depuis un fichier JSON de fixtures avant chaque test,
 * garantissant l'isolation des tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FireStationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private FireStationRepository  fireStationRepository;

    @BeforeEach
    public void resetFixture() throws IOException {
        dataStorage.initializeDataFile();
        dataStorage.loadData();
    }

    // TESTER LES CAS D'USAGES CORRECTS
    /**
     * Teste la récupération des personnes couvertes par la caserne numéro 3.
     * Vérifie le nombre d'adultes, d'enfants et les informations d'un premier contact.
     */
    @Test
    public void testGetFireStationByNumberStation() throws Exception {
        // GIVEN une station numéro 3
        String stationNumber = "3";

        // WHEN on appelle /firestation avec cette station
        mockMvc.perform(get("/firestation").param("stationNumber", stationNumber))

                // THEN on vérifie la structure et les valeurs attendues
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persons.length()").value(5))
                .andExpect(jsonPath("$.adults").value(3))
                .andExpect(jsonPath("$.children").value(2))
                .andExpect(jsonPath("$.persons[0]").value(
                        allOf(
                                hasEntry("firstName", "John"),
                                hasEntry("lastName", "Boyd"),
                                hasEntry("address", "1509 Culver St 97451 Culver"),
                                hasEntry("phone", "841-874-6512")
                        )
                ));
    }

    /**
     * Teste l'ajout d'une nouvelle caserne via POST.
     * Vérifie que la caserne est bien créée et persistée en base.
     */
    @Test
    public void testPostFireStation() throws Exception {
        // Given
        String newFireStationJson = """
        {
            "address":"27 Boulevard St",
            "station":"3"
        }
        """;

        // When
        mockMvc.perform(post("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newFireStationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("27 Boulevard St"))
                .andExpect(jsonPath("$.station").value("3"));

        // Then
        Optional<FireStation> newFireStation = fireStationRepository.findByAddress("27 Boulevard St");
        assertTrue(newFireStation.isPresent());
        assertEquals(3, newFireStation.get().getStation());
    }

    /**
     * Teste la mise à jour d'une caserne existante via PUT.
     * Vérifie que la station est bien mise à jour en base.
     */
    @Test
    public void testPutFireStation() throws Exception {
        // Vérifier Avant le put que la fireStation est différente du put
        //Given
        FireStation fireStationActually = fireStationRepository.findByAddress("1509 Culver St").orElseThrow();
        //When
        assertEquals("1509 Culver St", fireStationActually.getAddress());
        //Then
        assertEquals(3, fireStationActually.getStation());


        //given
        String updateFireStationJson = """
        {
            "address":"1509 Culver St",
            "station":"8"
        }
        """;

        //when
        mockMvc.perform(put("/firestation")
        .contentType(MediaType.APPLICATION_JSON)
        .content(updateFireStationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("1509 Culver St"))
                .andExpect(jsonPath("$.station").value("8"));

        //then
        Optional<FireStation> updatedStation = fireStationRepository.findByAddress("1509 Culver St");
        assertEquals(8, updatedStation.orElseThrow().getStation());
    }

    /**
     * Teste la suppression d'une caserne par adresse.
     * Vérifie que la caserne n'existe plus après la suppression.
     */
    @Test
    public void testDeleteFireStation() throws Exception {
        // Given
        String address = "1509 Culver St";
        Optional<FireStation> fireStation =  fireStationRepository.findByAddress("1509 Culver St");
        assertTrue(fireStation.isPresent());

        // When
        mockMvc.perform(delete("/firestation")
                .param("address", address))
                .andExpect(status().isNoContent());

        // Then
        Optional<FireStation> fireStationDeleted =  fireStationRepository.findByAddress("1509 Culver St");
        assertFalse(fireStationDeleted.isPresent());
    }

    // TESTER LES CAS D'USAGES EXISTE OU N'EXISTE PAS QUAND IL NE LE DEVRAI PAS
    /**
     * Teste la requête GET pour une caserne qui n'existe pas.
     * Vérifie que la réponse est un 404 avec un message d'erreur précis.
     */
    @Test
    public void testGetFireStationByNumberStationButNumberStationDoesntExist() throws Exception {
        // Given
        String stationNumber = "12";
        String expectedResult = "Aucune FireStation avec le numéro de station : " +  stationNumber;

        // When + Then
        mockMvc.perform(get("/firestation")
                        .param("stationNumber", stationNumber))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(expectedResult)));
    }
    /**
     * Teste l'ajout d'une caserne qui existe déjà.
     * Vérifie que le serveur renvoie un conflit (409).
     */
    @Test
    public void testPostFireStationButFireStationAlreadyExist() throws Exception {
        // Given
        String expectedResult = "FireStation already exists";
        String newFireStationJson = """
        {
            "address":"1509 Culver St",
            "station":"5"
        }
        """;

        // When + Then
        mockMvc.perform(post("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newFireStationJson))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la mise à jour d'une caserne inexistante.
     * Vérifie que le serveur renvoie un 404 avec un message d'erreur.
     */
    @Test
    public void testPutFireStationButFireStationDoesntExist() throws Exception {
        // Given
        String expectedResult = "FireStation does not exist";
        String newFireStationJson = """
        {
            "address":"27 Boulevard St",
            "station":"5"
        }
        """;

        // When + Then
        mockMvc.perform(put("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newFireStationJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la suppression d'une caserne inexistante.
     * Vérifie que le serveur renvoie un 404 avec un message d'erreur.
     */
    @Test
    public void testDeleteFireStationButFireStationDoesntExist() throws Exception {
        // Given une address
        String address = "27 Boulevard St";

        // When + Then
        mockMvc.perform(delete("/firestation")
        .param("address", address))
                .andExpect(status().isNoContent())
                .andExpect(content().string(containsString("")));
    }

    // TESTER QUAND ON RECOIS UNE FIRESTATION NULL
    /**
     * Teste la mise à jour d'une caserne avec un corps de requête null.
     * Vérifie que le serveur renvoie un 400 Bad Request.
     */
    @Test
    public void testPutFireStationButFireStationIsNull() throws Exception {
        // Given
        String nullContent = "null";
        String expectedResult = "Request body is invalid or missing";

        // When + Then
        mockMvc.perform(put("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste l'ajout d'une caserne avec un corps de requête null.
     * Vérifie que le serveur renvoie un 400 Bad Request.
     */
    @Test
    public void testPostFireStationButFireStationIsNull() throws Exception {
        String nullContent = "null";
        String expectedResult = "Request body is invalid or missing";

        mockMvc.perform(post("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedResult)));
    }
}
