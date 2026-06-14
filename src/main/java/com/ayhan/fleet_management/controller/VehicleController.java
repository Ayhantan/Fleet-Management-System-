package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.VehicleRequestDto;
import com.ayhan.fleet_management.dto.VehicleResponseDto;
import com.ayhan.fleet_management.dto.VehicleUsageUpdateRequestDto;
import com.ayhan.fleet_management.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponseDto> createVehicle(@Valid @RequestBody VehicleRequestDto requestDto) {
        VehicleResponseDto responseDto = vehicleService.createVehicle(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponseDto>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDto requestDto
    ) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, requestDto));
    }

    @PutMapping("/{vehicleId}/group/{groupId}")
    public ResponseEntity<VehicleResponseDto> assignGroup(
            @PathVariable Long vehicleId,
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(vehicleService.assignGroup(vehicleId, groupId));
    }

    @DeleteMapping("/{vehicleId}/group")
    public ResponseEntity<VehicleResponseDto> removeGroup(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.removeGroup(vehicleId));
    }

    @PatchMapping("/{vehicleId}/usage")
    public ResponseEntity<VehicleResponseDto> updateUsage(
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleUsageUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(vehicleService.updateUsage(vehicleId, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
