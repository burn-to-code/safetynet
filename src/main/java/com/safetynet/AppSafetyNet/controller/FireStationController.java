package com.safetynet.AppSafetyNet.controller;

import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.PersonCoveredDTO;
import com.safetynet.AppSafetyNet.service.FireStationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/firestation")
public class FireStationController {

    private final FireStationService fireStationService;

    public FireStationController(FireStationService fireStationService) {
        this.fireStationService = fireStationService;
    }

    @PostMapping
    public ResponseEntity<?> addFireStation(@RequestBody FireStation fs) {
        fireStationService.saveFireStation(fs);
        return ResponseEntity.status(HttpStatus.CREATED).body(fs);
    }

    @PutMapping()
    public ResponseEntity<?> updateFireStation(@RequestBody FireStation fs) {
        fireStationService.updateFireStation(fs);
        return ResponseEntity.ok(fs);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteFireStation(@RequestParam String address) {
        fireStationService.deleteFireStation(address);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getFireStation(@RequestParam String stationNumber) {
        PersonCoveredDTO response = fireStationService.getPersonCoveredByNumberStation(stationNumber);
        return ResponseEntity.ok(response);
    }
}
