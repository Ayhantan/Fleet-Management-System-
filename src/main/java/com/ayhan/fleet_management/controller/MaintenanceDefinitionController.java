package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.MaintenanceDefinitionRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceDefinitionResponseDto;
import com.ayhan.fleet_management.service.MaintenanceDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-definitions")
@RequiredArgsConstructor
public class MaintenanceDefinitionController {

    private final MaintenanceDefinitionService maintenanceDefinitionService;

    @PostMapping
    public ResponseEntity<MaintenanceDefinitionResponseDto> createMaintenanceDefinition(
            @Valid @RequestBody MaintenanceDefinitionRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(maintenanceDefinitionService.createMaintenanceDefinition(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceDefinitionResponseDto>> getAllMaintenanceDefinitions() {
        return ResponseEntity.ok(maintenanceDefinitionService.getAllMaintenanceDefinitions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceDefinitionResponseDto> getMaintenanceDefinitionById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceDefinitionService.getMaintenanceDefinitionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceDefinitionResponseDto> updateMaintenanceDefinition(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceDefinitionRequestDto requestDto
    ) {
        return ResponseEntity.ok(maintenanceDefinitionService.updateMaintenanceDefinition(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceDefinition(@PathVariable Long id) {
        maintenanceDefinitionService.deleteMaintenanceDefinition(id);
        return ResponseEntity.noContent().build();
    }
}
