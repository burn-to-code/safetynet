package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.service.PersonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;


    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    public ResponseEntity<?> addPerson(@RequestBody Person person) {
        personService.addPerson(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @PutMapping()
    public ResponseEntity<?> updatePerson(@RequestBody Person person ) {
         personService.updatePerson(person);
         return ResponseEntity.ok(person);
    }

    @DeleteMapping()
    // /person?firstName=xxx&lastName=YYY
    public ResponseEntity<?> deletePerson(@RequestParam  String firstName, @RequestParam  String lastName) {
        personService.removePerson(firstName, lastName);
        return ResponseEntity.noContent().build();
    }

}
