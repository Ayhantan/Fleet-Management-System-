package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.VehicleGroupRequestDto;
import com.ayhan.fleet_management.dto.VehicleGroupResponseDto;
import com.ayhan.fleet_management.service.VehicleGroupService;
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
@RequestMapping("/api/vehicle-groups")
@RequiredArgsConstructor
public class VehicleGroupController {

    private final VehicleGroupService vehicleGroupService;

    @PostMapping
    public ResponseEntity<VehicleGroupResponseDto> createVehicleGroup(
            @Valid @RequestBody VehicleGroupRequestDto requestDto
    ) {
        VehicleGroupResponseDto responseDto = vehicleGroupService.createVehicleGroup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<VehicleGroupResponseDto>> getAllVehicleGroups() {
        return ResponseEntity.ok(vehicleGroupService.getAllVehicleGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleGroupResponseDto> getVehicleGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleGroupService.getVehicleGroupById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleGroupResponseDto> updateVehicleGroup(
            @PathVariable Long id,
            @Valid @RequestBody VehicleGroupRequestDto requestDto
    ) {
        return ResponseEntity.ok(vehicleGroupService.updateVehicleGroup(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicleGroup(@PathVariable Long id) {
        vehicleGroupService.deleteVehicleGroup(id);
        return ResponseEntity.noContent().build();
    }
}
