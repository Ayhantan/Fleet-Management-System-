package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.MaintenanceScheduleRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceScheduleResponseDto;
import com.ayhan.fleet_management.service.MaintenanceScheduleService;
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
@RequestMapping("/api/maintenance-schedules")
@RequiredArgsConstructor
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService maintenanceScheduleService;

    @PostMapping
    public ResponseEntity<MaintenanceScheduleResponseDto> createMaintenanceSchedule(
            @Valid @RequestBody MaintenanceScheduleRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(maintenanceScheduleService.createMaintenanceSchedule(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceScheduleResponseDto>> getAllMaintenanceSchedules() {
        return ResponseEntity.ok(maintenanceScheduleService.getAllMaintenanceSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceScheduleResponseDto> getMaintenanceScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceScheduleService.getMaintenanceScheduleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceScheduleResponseDto> updateMaintenanceSchedule(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceScheduleRequestDto requestDto
    ) {
        return ResponseEntity.ok(maintenanceScheduleService.updateMaintenanceSchedule(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceSchedule(@PathVariable Long id) {
        maintenanceScheduleService.deleteMaintenanceSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/recalculate")
    public ResponseEntity<MaintenanceScheduleResponseDto> recalculateMaintenanceSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceScheduleService.recalculateMaintenanceSchedule(id));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MaintenanceScheduleResponseDto>> getUpcomingMaintenanceSchedules() {
        return ResponseEntity.ok(maintenanceScheduleService.getUpcomingMaintenanceSchedules());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<MaintenanceScheduleResponseDto>> getOverdueMaintenanceSchedules() {
        return ResponseEntity.ok(maintenanceScheduleService.getOverdueMaintenanceSchedules());
    }
}
