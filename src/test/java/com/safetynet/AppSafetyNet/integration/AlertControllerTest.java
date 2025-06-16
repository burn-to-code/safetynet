package com.safetynet.AppSafetyNet.integration;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
@TestPropertySource(properties = {
        "application.data-file-path=src/test/resources/fixtures/test-data.json"
})
@AutoConfigureMockMvc
public class AlertControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataStorage dataStorage;

    private String currentTestDataFile = "src/test/resources/fixtures/test-data.json.orig";

    @BeforeEach
    public void resetFixture() throws IOException {
        Files.copy(
                Path.of(currentTestDataFile),
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

    // CAS D'USAGE NORMAL

    /**
     * Teste la récupération des enfants vivant à l'adresse "1509 Culver St".
     * Vérifie que les informations des enfants et des personnes dans la même maison sont correctes.
     */
    @Test
    public void testGetChildrenAtAddress()  throws Exception {
        mockMvc.perform(get("/childAlert")
                .param("address","1509 Culver St"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].first_name").value("Tenley"))
                .andExpect(jsonPath("$[0].last_name").value("Boyd"))
                .andExpect(jsonPath("$[0].age").value(13))
                .andExpect(jsonPath("$[0].persons_in_same_house").isArray())
                .andExpect(jsonPath("$[0].persons_in_same_house").value(
                        org.hamcrest.Matchers.containsInAnyOrder(
                                "John Boyd", "Jacob Boyd", "Roger Boyd", "Felicia Boyd"
                        )
                ))
                .andExpect(jsonPath("$[1].first_name").value("Roger"))
                .andExpect(jsonPath("$[1].last_name").value("Boyd"))
                .andExpect(jsonPath("$[1].age").value(7))
                .andExpect(jsonPath("$[1].persons_in_same_house").value(
                        org.hamcrest.Matchers.containsInAnyOrder(
                                "John Boyd", "Jacob Boyd", "Tenley Boyd", "Felicia Boyd"
                        )
                ));
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
     * Teste que les endpoints renvoient une erreur 400 (Bad Request)
     * lorsque le paramètre est fourni mais vide (espace).
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
    public void testGetAllControllerWithParameterEmpty(String endPoint, String param) throws Exception {
        mockMvc.perform(get(endPoint)
                .param(param, " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(param + " must not be empty"));
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
     * Teste que la requête sur /phoneAlert avec un numéro de station inexistant renvoie une erreur 404.
     */
    @Test
    public void testGetPhoneAtAddressButTheStationNumberDoesntExist() throws Exception {
        mockMvc.perform(get("/phoneAlert")
        .param("numberFireStation", "12"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Fire station with number: 12, not found or covers no address"));
    }

    /**
     * Teste que la requête /fire avec une adresse inexistante renvoie une erreur 404.
     */
    @Test
    public void testGetFireAtAddressButPersonDoesntExistAtTheAddress() throws Exception {
        mockMvc.perform(get("/fire")
                .param("address", "addressDoesNotExist"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Any Person not found at addressDoesNotExist"));
    }


    /**
     * Teste que la requête /fire renvoie une erreur 404
     * si le dossier médical d'une personne est manquant dans les données.
     */
    @Test
    public void testGetFireAtAddressButMedicalRecordDoesntExistForOneOrMorePerson() throws Exception {
        currentTestDataFile = "src/test/resources/expected/fire/data-set-medical-record-missing.json";
        resetFixture();

        mockMvc.perform(get("/fire")
                .param("address", "1509 Culver St"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("An error is occurred : Medical Record with name John Boyd not found"));
    }

    /**
     * Teste que la requête /fire renvoie une erreur 404
     * si aucune caserne ne couvre l'adresse donnée.
     */
    @Test
    public void testGetFireAtAddressButFireStationDoesntExist() throws Exception {
        currentTestDataFile = "src/test/resources/expected/fire/data-set-fire-station-missing.json";
        resetFixture();

        mockMvc.perform(get("/fire")
                .param("address", "1509 Culver St"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Any Fire station covered: 1509 Culver St"));
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
                .andExpect(content().string("The StationNumber: [10, 8] not exist"));
    }

    /**
     * Teste que la requête /flood/stations renvoie une erreur 404
     * si aucune personne n'est trouvée pour les stations données.
     */
    @Test
    public void testGetFloodAtAddressButTheStationNumberCoveredNoPerson() throws Exception {
        currentTestDataFile = "src/test/resources/expected/flood/data-set-person-missing.json";
        resetFixture();

        mockMvc.perform(get("/flood/stations")
                        .param("stationNumber", "2")
                        .param("stationNumber", "4"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No person find for : [951 LoneTree Rd, 489 Manchester St]"));
    }

    /**
     * Teste que la requête /flood/stations renvoie une erreur 404
     * si un dossier médical est manquant pour une ou plusieurs personnes.
     */
    @Test
    public void testGetFloodAtAddressButMedicalRecordDoesntExistForOneOrMorePerson() throws Exception {
        currentTestDataFile = "src/test/resources/expected/flood/data-set-person-missing.json";
        resetFixture();

        mockMvc.perform(get("/flood/stations")
                        .param("stationNumber", "3"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Dossier médical manquant pour : John Boyd"));
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
        currentTestDataFile = "src/test/resources/expected/personInfoLastName/data-set-medical-record-missing.json";
        resetFixture();

        mockMvc.perform(get("/personInfoLastName")
                        .param("lastName", "Boyd"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No Medical Record found with Person: John Boyd"));
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
