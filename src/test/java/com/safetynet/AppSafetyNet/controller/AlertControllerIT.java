package com.safetynet.AppSafetyNet.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.AppSafetyNet.model.dto.ChildAlertDTO;
import com.safetynet.AppSafetyNet.model.dto.ResponseFireDTO;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * Tests d'intégration pour le contrôleur AlertController.
 * <p>
 * Ces tests vérifient les différents endpoints exposés pour les alertes liées
 * aux enfants, numéros de téléphone d'urgence, incendies, inondations, infos personnelles,
 * et emails communautaires.
 * <p>
 * Ils couvrent
 * - Les cas d'utilisation normaux avec des données valides.
 * - La gestion des paramètres requis absents ou vides.
 * - Les cas d'erreur spécifiques liés aux données manquantes ou incohérentes.
 * <p>
 * Les données utilisées sont rechargées depuis un fichier JSON de fixtures avant chaque test,
 * garantissant l'isolation des tests. Une variable du chemin permet de modifier le contenu à
 * tester avant chaque test, très pratique.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AlertControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void resetFixture() throws IOException {
        dataStorage.initializeDataFile();
        dataStorage.loadData();
    }

    // CAS D'USAGE NORMAL

    /**
     * Teste la récupération des enfants vivant à l'adresse "1509 Culver St".
     * Vérifie que les informations des enfants et des personnes dans la même maison sont correctes.
     */
    @Test
    public void testGetChildrenAtAddress() throws Exception {
        // GIVEN
        String address = "1509 Culver St";
        List<ChildAlertDTO> expected = List.of(
                new ChildAlertDTO("Tenley", "Boyd", 13, List.of("John Boyd", "Jacob Boyd", "Roger Boyd", "Felicia Boyd")),
                new ChildAlertDTO("Roger", "Boyd", 7, List.of("John Boyd", "Jacob Boyd", "Tenley Boyd", "Felicia Boyd"))
        );

        //WHEN
        MvcResult result = mockMvc.perform(get("/childAlert")
                        .param("address", address))
                .andExpect(status().isOk())
                .andReturn();

        //THEN
        String json = result.getResponse().getContentAsString();
        List<ChildAlertDTO> actual = objectMapper.readValue(
                json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ChildAlertDTO.class)
        );

        assertEquals(expected, actual);
    }

    /**
     * Teste la récupération des numéros de téléphone liés à la caserne de pompiers numéro 3.
     * Vérifie que la liste des téléphones retournée est correcte.
     */
    @Test
    public void testGetPhoneAtAddress() throws Exception {
        // GIVEN a station number
        final String numberFireStation = "3";
        final String[] expectedPhoneNumbers = {"841-874-6512","841-874-6513","841-874-6544"};

        // WHEN the phoneAlert is call
        final var response = mockMvc.perform(get("/phoneAlert")
                .param("numberFireStation", numberFireStation));

        // THEN the status is OK
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").value(containsInAnyOrder(expectedPhoneNumbers)
                ));
    }

    /**
     * Teste la récupération des informations d'incendie pour l'adresse "1509 Culver St".
     * Compare la réponse JSON complète avec un fichier attendu.
     */
    @Test
    public void testGetFireAtAddress() throws Exception {
        //given
        String address = "1509 Culver St";
        String expectedJson = Files.readString(Path.of("src/test/resources/expected/fire/response-fire-1509-culver.json"));

        //When
        MvcResult result = mockMvc.perform(get("/fire")
                .param("address", address))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();

        JSONAssert.assertEquals(expectedJson, jsonResponse, JSONCompareMode.LENIENT);
    }

    /**
     * Teste la récupération des informations d'inondation pour les stations 1 et 3.
     * Compare la réponse JSON complète avec un fichier attendu.
     */
    @Test
    public void testGetFloodAtAddress() throws Exception {
        //Given
        String stationNumber1 = "1";
        String stationNumber2 = "3";
        String expectedJson = Files.readString(Path.of("src/test/resources/expected/flood/response-flood-stationNumber-1-3.json"));

        //When
        MvcResult result = mockMvc.perform(get("/flood/stations")
                .param("stationNumber", stationNumber1)
                .param("stationNumber", stationNumber2))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();

        JSONAssert.assertEquals(expectedJson, jsonResponse, JSONCompareMode.LENIENT);
    }

    /**
     * Teste la récupération des informations des personnes par nom de famille "Zemicks".
     * Compare la réponse JSON complète avec un fichier attendu.
     */
    @Test
    public void testGetPersonInfoLastName() throws Exception {
        // given
        String lastName = "Zemicks";
        String expectedJson = Files.readString(Path.of("src/test/resources/expected/personInfoLastName/response-PersonInfoLastName-Zemicks.json"));

        // When
        MvcResult result = mockMvc.perform(get("/personInfoLastName")
                .param("lastName", lastName))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();

        JSONAssert.assertEquals(expectedJson, jsonResponse, JSONCompareMode.LENIENT);
    }

    /**
     * Teste la récupération des emails communautaires pour la ville "Culver".
     * Vérifie que la liste des emails retournée correspond aux emails attendus.
     */
    @Test
    public void testGetCommunityEmailByCity() throws Exception {
        // GIVEN une ville "Culver"
        final String[] expectedEmails = {
                "jaboyd@email.com", "drk@email.com", "tenz@email.com",
                "soph@email.com", "ward@email.com", "zarc@email.com",
                "bstel@email.com", "ssanw@email.com"
        };

        // WHEN + THEN
        mockMvc.perform(get("/communityEmail").param("city", "Culver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(containsInAnyOrder(expectedEmails)));
    }

    @Test
    public void testGetCommunityEmailByCityButNoPersonFindAtTheCity() throws Exception {
        // given
        String expectedResponse = "No Email found with City: Toulouse";
        String city = "Toulouse";

        // When + Then
        mockMvc.perform(get("/communityEmail")
                .param("city",city))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedResponse));
    }

    // CAS OU LES PARAMS SONT ABSENT(NULL)
    /**
     * Teste que les endpoints renvoient une erreur 400 (Bad Request)
     * lorsque le paramètre requis est absent.
     */
    @ParameterizedTest
    @CsvSource({
            "/childAlert,address",
            "/phoneAlert,numberFireStation",
            "/fire,address",
            "/flood/stations,stationNumber",
            "/personInfoLastName,lastName",
            "/communityEmail,city"
    })
    public void testGetChildrenAtAddressButAddressIsNull(String endPoint, String missingParam ) throws Exception {
        // given
        String expectedResponse = "Missing required parameter: " + missingParam;

        // When + Then
        mockMvc.perform(get(endPoint))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedResponse));
    }

    // TESTER LES EMPTY
    /**
     * Teste que les endpoints renvoient une erreur 400 (Bad Request) (Hors Phone Alert car Integer en param)
     * lorsque le paramètre est fourni, mais vide (espace).
     */
    @ParameterizedTest
    @CsvSource({
            "/childAlert,address,empty",
            "/fire,address,empty",
            "/flood/stations,stationNumber,null",
            "/personInfoLastName,lastName,empty",
            "/communityEmail,city,empty"
    })
    public void testGetAllControllerWithParameterEmpty(String endPoint, String param, String type) throws Exception {
        // Given
        String expectedResponse = param + " must not be " + type;
        String emptyParam = " ";

        // When + Then
        mockMvc.perform(get(endPoint)
                .param(param, emptyParam))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedResponse ));
    }


    /**
     * Test de AlertPhone avec le param Integer en String Empty : doit renvoyer une exception grâce au global controller
     * @throws Exception lève une exception si perform ne fonctionne pas
     */
    @Test
    public void testGetPhoneAlertWithParameterEmpty() throws Exception {
        // given
        String expectedResponse = "Missing required parameter: numberFireStation";
        String emptyParam = " ";

        // When + Then
        mockMvc.perform(get("/phoneAlert")
                .param("numberFireStation", emptyParam))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedResponse));
    }

    // TESTER LES CAS PLUS PARTICULIER
    /**
     * Teste que l'endpoint /childAlert renvoie une réponse vide
     * lorsque aucune donnée n'est trouvée pour l'adresse donnée.
     */
    @Test
    public void testGetChildrenAtAddressButShouldReturnNone() throws Exception {
        // given
        String addressDoesNotExist = "addressDoesNotExist";

        // When + Then
        mockMvc.perform(get("/childAlert")
                        .param("address", addressDoesNotExist))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }


    /**
     * Teste que la requête sur /phoneAlert
     */
    @Test
    public void testGetPhoneAlertAtAddressWithStationNumberButTheStationNumberDoesntHaveFireStation() throws Exception {
        // given
        String numberFireStationWhoNotExist = "12";

        // When + Then
        mockMvc.perform(get("/phoneAlert")
        .param("numberFireStation", numberFireStationWhoNotExist))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    /**
     * Teste que la requête /fire
     */
    @Test
    @Tag("/fire")
    public void testGetFireAtAddressButPersonDoesntExistAndFireStationDoesntExistAtTheAddress() throws Exception {
        // Given
        String addressDoesNotExist = "addressDoesNotExist";
        String expectedResponse = "Aucune Données n'as été trouvé pour l'adresse: " + addressDoesNotExist;

        // When + Then
        mockMvc.perform(get("/fire")
                .param("address", addressDoesNotExist))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    @Tag("/fire")
    public void testGetFireAtAddressButPersonDoesntExistAtTheAddressButFireStationExist() throws Exception {
        // Given
        ObjectMapper objectMapper = new ObjectMapper();
        String address = "489 Manchester St";

        // When
        MvcResult mvc = mockMvc.perform(get("/fire")
                .param("address", address))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String jsonResponse = mvc.getResponse().getContentAsString();
        ResponseFireDTO response = objectMapper.readValue(jsonResponse, ResponseFireDTO.class);

        assertNotNull(response);
        assertTrue(response.persons().isEmpty(), "La liste persons doit être vide");
        assertEquals(4, response.stationNumber());
    }

    @Test
    @Tag("/fire")
    public void testGetFireAtAddressButPersonExistAtTheAddressButFireStationDoesntExist() throws Exception {
        // Given
        ObjectMapper objectMapper = new ObjectMapper();
        String address = "1 tour eiffel";

        // When
        MvcResult mvc = mockMvc.perform(get("/fire")
                        .param("address", address))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvc.getResponse().getContentAsString();
        ResponseFireDTO response = objectMapper.readValue(jsonResponse, ResponseFireDTO.class);

        assertNotNull(response);
        assertFalse(response.persons().isEmpty(), "La liste persons doit contenir quelqu'un");
        assertNull(response.stationNumber(), "il ne doit pas y avoir de station number a cet endroit = null");
    }

    @Test
    @Tag("/fire")
    public void testGetFireAtAddressPersonAndFireStationExistAtTheAddressButNotMedicalRecordForPerson() throws Exception {
        // Given
        String expectedResult = "An error is occurred : Medical Record with name Daniel SansDossierMedical not found";
        String address = "100 tour eiffel";

        // When + Then
        mockMvc.perform(get("/fire")
                        .param("address", address))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(expectedResult));
    }


    /**
     * Teste que la requête /flood/stations avec des numéros de station inexistants renvoie une erreur 404.
     */
    @Test
    public void testGetFloodAtAddressButTheStationNumberDoesntExist() throws Exception {
        // Given
        String numberFireStation1 = "10";
        String numberFireStation2 = "8";
        String expectedResponse = "Aucune FireStations n'existe avec les numéros de station: [10, 8]";

        // When + Then
        mockMvc.perform(get("/flood/stations")
                .param("stationNumber", numberFireStation1)
                .param("stationNumber", numberFireStation2))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedResponse));
    }

    /**
     * Teste que la requête /flood/stations renvoie une erreur 404
     * si aucune personne n'est trouvée pour les stations données.
     */
    @Test
    public void testGetFloodAtAddressButTheStationNumberCoveredNoPerson() throws Exception {
        // Given
        String expectedAddress = "1 Boulevard Carnot";
        String stationNumber = "9";

        // When + Then
        mockMvc.perform(get("/flood/stations")
                        .param("stationNumber", stationNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address").value(expectedAddress))
                .andExpect(jsonPath("$[0].personInfo").isEmpty());
    }

    /**
     * Teste que la requête /flood/stations renvoie une erreur 404
     * si un dossier médical est manquant pour une ou plusieurs personnes.
     */
    @Test
    public void testGetFloodAtAddressButMedicalRecordDoesntExistForOneOrMorePerson() throws Exception {
        //Given
        String expectedAddress = "Une erreur est survenue : Dossier médical manquant pour : Daniel SansDossierMedical";
        String stationNumber = "5";

        //When + Then
        mockMvc.perform(get("/flood/stations")
                        .param("stationNumber", stationNumber))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(expectedAddress));
    }

    /**
     * Teste que la requête /personInfoLastName renvoie une erreur 404
     * si aucune personne avec le nom de famille donné n'est trouvée.
     */
    @Test
    public void testGetPersonInfoLastNameButNoPersonExist() throws Exception {
        // Given
        String lastName = "Garnier";
        String expectedResponse = "No Person found with lastName: " + lastName;

        // When + Then
        mockMvc.perform(get("/personInfoLastName")
        .param("lastName",lastName))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedResponse));
    }

    /**
     * Teste que la requête /personInfoLastName renvoie une erreur 404
     * si aucun dossier médical n'est trouvé pour une ou plusieurs personnes.
     */
    @Test
    public void testGetPersonInfoLastNameButMedicalRecordForOnePersonOrMoreDoestExist() throws Exception {
        // Given
        String lastName = "SansDossierMedical";
        String expectedResponse = "Dossier médical introuvable pour: Daniel SansDossierMedical";

        // When + Then
        mockMvc.perform(get("/personInfoLastName")
                        .param("lastName", lastName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(expectedResponse));
    }


    /**
     * Teste que la requête /communityEmail renvoie une erreur 404
     * si aucun email n'est trouvé pour la ville donnée.
     */
    @Test
    public void testGetPersonInfoLastNameButMedicalRecordDoesntExist() throws Exception {
        // Given
        String city = "Miami";
        String expectedResponse = "No Email found with City: Miami";

        // When + Then
        mockMvc.perform(get("/communityEmail")
        .param("city", city))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedResponse));
    }
}
