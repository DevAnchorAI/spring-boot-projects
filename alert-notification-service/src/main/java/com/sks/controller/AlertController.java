package com.sks.controller;

import com.sks.dto.AlertRequest;
import com.sks.dto.AlertResponse;
import com.sks.entity.AlertStatus;
import com.sks.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@Valid @RequestBody AlertRequest request) {
        AlertResponse response = alertService.createAlert(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlert(@PathVariable Long id) {
        AlertResponse response = alertService.getAlert(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts() {
        List<AlertResponse> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<List<AlertResponse>> getAlertsByEmail(@PathVariable String email) {
        List<AlertResponse> alerts = alertService.getAlertsByEmail(email);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<AlertResponse>> getAlertsByStatus(@PathVariable AlertStatus status) {
        List<AlertResponse> alerts = alertService.getAlertsByStatus(status);
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledgeAlert(
            @PathVariable Long id,
            @RequestParam String acknowledgedBy) {
        AlertResponse response = alertService.acknowledgeAlert(id, acknowledgedBy);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}

