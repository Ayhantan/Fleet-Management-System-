package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.MaintenanceTaskCompletionRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceTaskRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceTaskResponseDto;
import com.ayhan.fleet_management.service.MaintenanceTaskService;
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
@RequestMapping("/api/maintenance-tasks")
@RequiredArgsConstructor
public class MaintenanceTaskController {

    private final MaintenanceTaskService maintenanceTaskService;

    @PostMapping
    public ResponseEntity<MaintenanceTaskResponseDto> createMaintenanceTask(
            @Valid @RequestBody MaintenanceTaskRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(maintenanceTaskService.createMaintenanceTask(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceTaskResponseDto>> getAllMaintenanceTasks() {
        return ResponseEntity.ok(maintenanceTaskService.getAllMaintenanceTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceTaskResponseDto> getMaintenanceTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceTaskService.getMaintenanceTaskById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceTaskResponseDto> updateMaintenanceTask(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceTaskRequestDto requestDto
    ) {
        return ResponseEntity.ok(maintenanceTaskService.updateMaintenanceTask(id, requestDto));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<MaintenanceTaskResponseDto> completeMaintenanceTask(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceTaskCompletionRequestDto requestDto
    ) {
        return ResponseEntity.ok(maintenanceTaskService.completeMaintenanceTask(id, requestDto));
    }

    @GetMapping("/history/vehicle/{vehicleId}")
    public ResponseEntity<List<MaintenanceTaskResponseDto>> getVehicleMaintenanceHistory(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(maintenanceTaskService.getVehicleMaintenanceHistory(vehicleId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceTask(@PathVariable Long id) {
        maintenanceTaskService.deleteMaintenanceTask(id);
        return ResponseEntity.noContent().build();
    }
}
