package com.safetynet.AppSafetyNet;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        List<ChildAlertDTO> expected = List.of(
                new ChildAlertDTO("Tenley", "Boyd", 13, List.of("John Boyd", "Jacob Boyd", "Roger Boyd", "Felicia Boyd")),
                new ChildAlertDTO("Roger", "Boyd", 7, List.of("John Boyd", "Jacob Boyd", "Tenley Boyd", "Felicia Boyd"))
        );

        MvcResult result = mockMvc.perform(get("/childAlert")
                        .param("address", "1509 Culver St"))
                .andExpect(status().isOk())
                .andReturn();

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
        mockMvc.perform(get("/phoneAlert")
                .param("numberFireStation", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(
                        org.hamcrest.Matchers.containsInAnyOrder(
                                "841-874-6512","841-874-6513","841-874-6544"
                        )
                ));
    }

    /**
     * Teste la récupération des informations d'incendie pour l'adresse "1509 Culver St".
     * Compare la réponse JSON complète avec un fichier attendu.
     */
    @Test
    public void testGetFireAtAddress() throws Exception {
        MvcResult result = mockMvc.perform(get("/fire")
                .param("address", "1509 Culver St"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        String expectedJson = Files.readString(Path.of("src/test/resources/expected/fire/response-fire-1509-culver.json"));

        JSONAssert.assertEquals(expectedJson, jsonResponse, JSONCompareMode.LENIENT);
    }

    /**
     * Teste la récupération des informations d'inondation pour les stations 1 et 3.
     * Compare la réponse JSON complète avec un fichier attendu.
     */
    @Test
    public void testGetFloodAtAddress() throws Exception {
        MvcResult result = mockMvc.perform(get("/flood/stations")
                .param("stationNumber", "1")
                .param("stationNumber", "3"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        String expectedJson = Files.readString(Path.of("src/test/resources/expected/flood/response-flood-stationNumber-1-3.json"));

        JSONAssert.assertEquals(expectedJson, jsonResponse, JSONCompareMode.LENIENT);
    }

    /**
     * Teste la récupération des informations des personnes par nom de famille "Zemicks".
     * Compare la réponse JSON complète avec un fichier attendu.
     */
    @Test
    public void testGetPersonInfoLastName() throws Exception {
        MvcResult result = mockMvc.perform(get("/personInfoLastName")
                .param("lastName", "Zemicks"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        String expectedJson = Files.readString(Path.of("src/test/resources/expected/personInfoLastName/response-PersonInfoLastName-Zemicks.json"));

        JSONAssert.assertEquals(expectedJson, jsonResponse, JSONCompareMode.LENIENT);
    }

    /**
     * Teste la récupération des emails communautaires pour la ville "Culver".
     * Vérifie que la liste des emails retournée correspond aux emails attendus.
     */
    @Test
    public void testGetCommunityEmailByCity() throws Exception {
        mockMvc.perform(get("/communityEmail")
                .param("city","Culver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("jaboyd@email.com"))
                .andExpect(jsonPath("$[1]").value("drk@email.com"))
                .andExpect(jsonPath("$[2]").value("tenz@email.com"))
                .andExpect(jsonPath("$[3]").value("soph@email.com"))
                .andExpect(jsonPath("$[4]").value("ward@email.com"))
                .andExpect(jsonPath("$[5]").value("zarc@email.com"))
                .andExpect(jsonPath("$[6]").value("bstel@email.com"))
                .andExpect(jsonPath("$[7]").value("ssanw@email.com"));
    }

    @Test
    public void testGetCommunityEmailByCityButNoPersonFindAtTheCity() throws Exception {
        mockMvc.perform(get("/communityEmail")
                .param("city","Toulouse"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No Email found with City: Toulouse"));
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
        mockMvc.perform(get(endPoint))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing required parameter: " + missingParam));
    }

    // TESTER LES EMPTY
    /**
     * Teste que les endpoints renvoient une erreur 400 (Bad Request) (Hors Phone Alert car Integer en param)
     * lorsque le paramètre est fourni mais vide (espace).
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
        mockMvc.perform(get(endPoint)
                .param(param, " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(param + " must not be " + type ));
    }


    /**
     * Test de AlertPhone avec le param Integer en String Empty : doit renvoyer une exception grâce au global controller
     * @throws Exception lève une exception si perform ne fonctionne pas
     */
    @Test
    public void testGetPhoneAlertWithParameterEmpty() throws Exception {
        mockMvc.perform(get("/phoneAlert")
                .param("numberFireStation", " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing required parameter: numberFireStation"));
    }

    // TESTER LES CAS PLUS PARTICULIER
    /**
     * Teste que le endpoint /childAlert renvoie une réponse vide
     * lorsque aucune donnée n'est trouvée pour l'adresse donnée.
     */
    @Test
    public void testGetChildrenAtAddressButShouldReturnNone() throws Exception {
        String addressDoesNotExist = "addressDoesNotExist";
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
        mockMvc.perform(get("/phoneAlert")
        .param("numberFireStation", "12"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    /**
     * Teste que la requête /fire
     */
    @Test
    @Tag("/fire")
    public void testGetFireAtAddressButPersonDoesntExistAndFireStationDoesntExistAtTheAddress() throws Exception {
        mockMvc.perform(get("/fire")
                .param("address", "addressDoesNotExist"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Aucune Données n'as été trouvé pour l'adresse: addressDoesNotExist"));
    }

    @Test
    @Tag("/fire")
    public void testGetFireAtAddressButPersonDoesntExistAtTheAddressButFireStationExist() throws Exception {
        MvcResult mvc = mockMvc.perform(get("/fire")
                .param("address", "489 Manchester St"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvc.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseFireDTO response = objectMapper.readValue(jsonResponse, ResponseFireDTO.class);

        assertNotNull(response);
        assertTrue(response.persons().isEmpty(), "La liste persons doit être vide");
        assertEquals(4, response.stationNumber());
    }

    @Test
    @Tag("/fire")
    public void testGetFireAtAddressButPersonExistAtTheAddressButFireStationDoesntExist() throws Exception {
        MvcResult mvc = mockMvc.perform(get("/fire")
                        .param("address", "1 tour eiffel"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvc.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseFireDTO response = objectMapper.readValue(jsonResponse, ResponseFireDTO.class);

        assertNotNull(response);
        assertFalse(response.persons().isEmpty(), "La liste persons doit contenir quelqu'un");
        assertNull(response.stationNumber(), "il ne doit pas y avoir de station number a cet endroit = null");
    }

    @Test
    @Tag("/fire")
    public void testGetFireAtAddressPersonAndFireStationExistAtTheAddressButNotMedicalRecordForPerson() throws Exception {
        mockMvc.perform(get("/fire")
                        .param("address", "100 tour eiffel"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error is occurred : Medical Record with name Daniel SansDossierMedical not found"));
    }


    /**
     * Teste que la requête /flood/stations avec des numéros de station inexistants renvoie une erreur 404.
     */
    @Test
    public void testGetFloodAtAddressButTheStationNumberDoesntExist() throws Exception {
        mockMvc.perform(get("/flood/stations")
                .param("stationNumber", "10")
                .param("stationNumber", "8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Aucune FireStations n'existe avec les numéros de station: [10, 8]"));
    }

    /**
     * Teste que la requête /flood/stations renvoie une erreur 404
     * si aucune personne n'est trouvée pour les stations données.
     */
    @Test
    public void testGetFloodAtAddressButTheStationNumberCoveredNoPerson() throws Exception {

        mockMvc.perform(get("/flood/stations")
                        .param("stationNumber", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address").value("1 Boulevard Carnot"))
                .andExpect(jsonPath("$[0].personInfo").isEmpty());
    }

    /**
     * Teste que la requête /flood/stations renvoie une erreur 404
     * si un dossier médical est manquant pour une ou plusieurs personnes.
     */
    @Test
    public void testGetFloodAtAddressButMedicalRecordDoesntExistForOneOrMorePerson() throws Exception {
        mockMvc.perform(get("/flood/stations")
                        .param("stationNumber", "5"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Une erreur est survenue : Dossier médical manquant pour : Daniel SansDossierMedical"));
    }

    /**
     * Teste que la requête /personInfoLastName renvoie une erreur 404
     * si aucune personne avec le nom de famille donné n'est trouvée.
     */
    @Test
    public void testGetPersonInfoLastNameButNoPersonExist() throws Exception {
        mockMvc.perform(get("/personInfoLastName")
        .param("lastName", "Garnier"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No Person found with lastName: Garnier"));
    }

    /**
     * Teste que la requête /personInfoLastName renvoie une erreur 404
     * si aucun dossier médical n'est trouvé pour une ou plusieurs personnes.
     */
    @Test
    public void testGetPersonInfoLastNameButMedicalRecordForOnePersonOrMoreDoestExist() throws Exception {
        mockMvc.perform(get("/personInfoLastName")
                        .param("lastName", "SansDossierMedical"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Dossier médical introuvable pour: Daniel SansDossierMedical"));
    }


    /**
     * Teste que la requête /communityEmail renvoie une erreur 404
     * si aucun email n'est trouvé pour la ville donnée.
     */
    @Test
    public void testGetPersonInfoLastNameButMedicalRecordDoesntExist() throws Exception {
        mockMvc.perform(get("/communityEmail")
        .param("city", "Miami"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No Email found with City: Miami"));
    }
}
