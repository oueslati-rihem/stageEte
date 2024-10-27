package com.authentifcation.projectpitwo.controller;


import com.authentifcation.projectpitwo.entities.Participationevent;
import com.authentifcation.projectpitwo.serviceInterface.Participation2Interface;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/part")
@CrossOrigin
public class Participation2Controller {
    Participation2Interface participationInterface;
    @PostMapping("/{NumEvent}/{Id}")
    public ResponseEntity<?> participate(@PathVariable Integer Id, @PathVariable Long NumEvent) {
        try {
            Participationevent participation = participationInterface.participate(Id, NumEvent);
            return ResponseEntity.status(HttpStatus.CREATED).body(participation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to participate: " + e.getMessage());
        }
    }

    @PutMapping("/accept/{IdPart}")
    public ResponseEntity<?> acceptParticipation(@PathVariable Long IdPart) {
        try {
            participationInterface.acceptParticipation(IdPart);
            return ResponseEntity.ok("Participation accepted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to accept participation: " + e.getMessage());
        }
    }

        @DeleteMapping("/reject/{IdPart}")
        public ResponseEntity<?> rejectParticipation (@PathVariable Long IdPart){
            try {
                participationInterface.rejectParticipation(IdPart);
                return ResponseEntity.ok("Participation rejected successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to reject participation: " + e.getMessage());
            }
        }

        @PostMapping("/{IdPart}/archive")
        public ResponseEntity<?> archiveParticipation (@PathVariable Long IdPart){
            try {
                Participationevent participation = participationInterface.archiveParticipation(IdPart);
                return ResponseEntity.ok("Participation with ID " + IdPart + " archived successfully.");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to archive participation: " + e.getMessage());
            }
        }

    @GetMapping("/participation")
    public ResponseEntity<List<Participationevent>> getParticipation() {
        List<Participationevent> participation = participationInterface.getParticipation();
        return ResponseEntity.ok(participation);
    }

    @GetMapping("/user/{Id}")
    public ResponseEntity<List<Participationevent>> getParticipationByUserId(@PathVariable Integer Id) {
        List<Participationevent> participation = participationInterface.getParticipationsByUserId(Id);
        return ResponseEntity.ok(participation);
    }
    @GetMapping("/eventcreator/{Id}")
    public ResponseEntity<List<Participationevent>> getParticipationByEventCreatorId(@PathVariable Integer Id) {
        List<Participationevent> participation = participationInterface.getParticipationsByEventCreatorId(Id);
        return ResponseEntity.ok(participation);
    }

    }

