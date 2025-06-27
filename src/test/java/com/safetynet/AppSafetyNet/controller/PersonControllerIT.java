package com.safetynet.AppSafetyNet.controller;
import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.repository.PersonRepository;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Classe de tests d'intégration pour le contrôleur PersonController.
 * <p>
 * Cette classe teste les endpoints relatifs aux opérations CRUD sur les entités Person.
 * <p>
 * Les scénarios couverts comprennent
 * - L'ajout, la modification, et la suppression d'une personne.
 * - La gestion des cas où la personne existe déjà ou n'existe pas pour certaines opérations.
 * - La validation des champs obligatoires (notamment le prénom).
 * - La gestion des requêtes malformées ou avec corps null.
 * <p>
 * Les données utilisées sont rechargées depuis un fichier JSON de fixtures avant chaque test,
 * garantissant l'isolation des tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PersonControllerIT {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void resetFixture() throws IOException {
        dataStorage.initializeDataFile();
        dataStorage.loadData();
    }

    // CAS D'USAGE NORMAL

    /**
     * Teste la création d'une nouvelle personne via POST.
     * Vérifie que la personne est bien ajoutée avec les données attendues.
     */
    @Test
    public void testPostPerson() throws Exception {
        // GIVEN une nouvelle personne à ajouter
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

        // WHEN on appelle POST /person avec cette personne
        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPersonJson))

                // THEN on vérifie que la personne est créée avec les bons attributs
                .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.firstName").value("Zikon"))
                    .andExpect(jsonPath("$.lastName").value("Neodal"))
                    .andExpect(jsonPath("$.address").value("892 Downing Ct"))
                    .andExpect(jsonPath("$.city").value("Culver"))
                    .andExpect(jsonPath("$.zip").value("97451"))
                    .andExpect(jsonPath("$.phone").value("841-874-7512"))
                    .andExpect(jsonPath("$.email").value("zarchino@email.com"));
    }
    /**
     * Teste la mise à jour d'une personne existante via PUT.
     * Vérifie que les modifications sont bien prises en compte.
     */
    @Test
    public void testPutPerson() throws Exception {
        // Given une personne à mettre à jour
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

        // When
        mockMvc.perform(put("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatePerson))
                // Then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Boyd"))
            .andExpect(jsonPath("$.email").value("test@email.com"));
    }


    /**
     * Teste la suppression d'une personne via DELETE.
     * Vérifie que la personne est bien supprimée de la base.
     */
    @Test
    public void testDeletePerson() throws Exception {
        // Given
        Optional<Person> Person = personRepository.findByFirstNameAndLastName("John", "Boyd");
        assertTrue(Person.isPresent());
        String firstName = "John";
        String lastName = "Boyd";

        //When
        mockMvc.perform(delete("/person")
            .param("firstName", firstName)
            .param("lastName", lastName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Then
        Optional<Person> personDeleted = personRepository.findByFirstNameAndLastName(firstName, lastName);
        assertFalse(personDeleted.isPresent());
    }

    // CAS D'USAGE OU LA PERSONNE N'EXISTE PAS OU EXISTE QUAND IL NE LE FAUT PAS

    /**
     * Teste la création d'une personne déjà existante.
     * Vérifie que le serveur répond avec un conflit (409).
     */
    @Test
    public void testPostPersonButAlreadyExists() throws Exception {
        // Given
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
        String expectedResult = "Person already exists";

        // when
        mockMvc.perform(post("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .content(personAlreadyExist))
                // then
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString(expectedResult)));

    }

    /**
     * Teste la mise à jour d'une personne qui n'existe pas.
     * Vérifie que le serveur répond avec un 404 Not Found.
     */
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
        String expectedResult = "Person not found : Michel Garnier";

        mockMvc.perform(put("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .content(personDoesntExist))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la suppression d'une personne qui n'existe pas.
     * Vérifie que le serveur répond avec un 404 Not Found.
     */
    @Test
    public void testDeletePersonButNotExists() throws Exception {
        // given
        String firstName = "Michel";
        String lastName = "Garnier";

        // when
        mockMvc.perform(delete("/person")
            .param("firstName", firstName)
            .param("lastName", lastName)
            .contentType(MediaType.APPLICATION_JSON))
                // then
            .andExpect(status().isNoContent())
            .andExpect(content().string(containsString("")));
    }

    // TESTER QUAND ON RENTRE UNE PERSON NULL

    /**
     * Teste la création d'une personne avec un prénom null.
     * Vérifie que le serveur répond avec un 400 Bad Request et un message d'erreur.
     */
    @Test
    public void testPostPersonButFirstNameIsNull() throws Exception {
        // given
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
        String expectedResult = "First name must not be null";

        //when
        mockMvc.perform(post("/person")
        .contentType(MediaType.APPLICATION_JSON)
        .content(personDoesntExist))
                // then
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la création d'une personne avec un corps de requête null.
     * Vérifie que le serveur répond avec un 400 Bad Request.
     */
    @Test
    public void testPostPersonButIsNull() throws Exception {
        // given
        String content = "null";
        String expectedResult = "Request body is invalid or missing";
        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedResult)));
    }

    /**
     * Teste la mise à jour d'une personne avec un corps de requête null.
     * Vérifie que le serveur répond avec un 400 Bad Request.
     */
    @Test
    public void testPutPersonButIsNull() throws Exception {
        // given
        String content = "null";
        String expectedResult = "Request body is invalid or missing";
        mockMvc.perform(put("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedResult)));
    }
}
