package com.safetynet.AppSafetyNet.integration;
import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "application.data-file-path=src/test/resources/fixtures/test-data.json"
})
@AutoConfigureMockMvc
public class PersonControllerTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private MockMvc mockMvc;

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

    // CAS D'USAGE NORMAL

    @Test
    public void testPostPerson() throws Exception {
        String newPersonJson = """
        {
            "firstName":"Zikon",
            "lastName":"Neodal",
            "address":"892 Downing Ct",
            "city":"Culver",
            "zip":"97451",
            "phone":"841-874-7512",
            "email":"zarchino@email.com"
        }
        """;
        mockMvc.perform(post("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPersonJson))
                .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.firstName").value("Zikon"))
                    .andExpect(jsonPath("$.lastName").value("Neodal"))
                    .andExpect(jsonPath("$.address").value("892 Downing Ct"))
                    .andExpect(jsonPath("$.city").value("Culver"))
                    .andExpect(jsonPath("$.zip").value("97451"))
                    .andExpect(jsonPath("$.phone").value("841-874-7512"))
                    .andExpect(jsonPath("$.email").value("zarchino@email.com"));
    }

    @Test
    public void testPutPerson() throws Exception {
        String updatePerson = """
                {
                    "firstName":"John",
                    "lastName":"Boyd",
                    "address":"1509 Culver St",
                    "city":"Culver",
                    "zip":"97451",
                    "phone":"841-874-6512",
                    "email":"test@email.com"
                }
                """;
        mockMvc.perform(put("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatePerson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Boyd"))
            .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    public void testDeletePerson() throws Exception {
        // Vérifier que la personne est présente avant
        Optional<Person> Person = personRepository.findByFirstNameAndLastName("John", "Boyd");
        assertTrue(Person.isPresent());

        //Supprimer la personne et obtenir la réponse http attendu
        mockMvc.perform(delete("/person")
            .param("firstName", "John")
            .param("lastName", "Boyd")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Vérifier que la personne n'est plus dans notre json
        Optional<Person> personDeleted = personRepository.findByFirstNameAndLastName("John", "Boyd");
        assertFalse(personDeleted.isPresent());
    }

    // CAS D'USAGE OU LA PERSONNE N'EXISTE PAS OU EXISTE QUAND IL NE LE FAUT PAS

    @Test
    public void testPostPersonButAlreadyExists() throws Exception {
        String personAlreadyExist = """
                {
                    "firstName":"John",
                    "lastName":"Boyd",
                    "address":"1509 Culver St",
                    "city":"Culver",
                    "zip":"97451",
                    "phone":"841-874-6512",
                    "email":"test@email.com"
                }
                """;

        mockMvc.perform(post("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .content(personAlreadyExist))
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("Person already exists")));

    }

    @Test
    public void testPutPersonButNotExists() throws Exception {
        String personDoesntExist = """
                {
                    "firstName":"Michel",
                    "lastName":"Garnier",
                    "address":"1509 Culver St",
                    "city":"Culver",
                    "zip":"97451",
                    "phone":"841-874-6512",
                    "email":"test@email.com"
                }
                """;
        mockMvc.perform(put("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .content(personDoesntExist))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Person not found : Michel Garnier")));
    }

    @Test
    public void testDeletePersonButNotExists() throws Exception {
        mockMvc.perform(delete("/person")
            .param("firstName", "Michel")
            .param("lastName", "Garnier")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Person with name Michel Garnier not found")));
    }

    // TESTER QUAND ON RENTRE UNE PERSON NULL
    @Test
    public void testPostPersonButFirstNameIsNull() throws Exception {
        String personDoesntExist = """
                {
                    "firstName": null,
                    "lastName":"Garnier",
                    "address":"1509 Culver St",
                    "city":"Culver",
                    "zip":"97451",
                    "phone":"841-874-6512",
                    "email":"test@email.com"
                }
                """;
        mockMvc.perform(post("/person")
        .contentType(MediaType.APPLICATION_JSON)
        .content(personDoesntExist))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("First name must not be null")));
    }

    @Test
    public void testPostPersonButIsNull() throws Exception {
        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Request body is invalid or missing")));
    }

    @Test
    public void testPutPersonButIsNull() throws Exception {
        mockMvc.perform(put("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Request body is invalid or missing")));
    }
}
