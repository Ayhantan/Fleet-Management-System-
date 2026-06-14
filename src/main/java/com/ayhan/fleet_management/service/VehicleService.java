package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.VehicleRequestDto;
import com.ayhan.fleet_management.dto.VehicleResponseDto;
import com.ayhan.fleet_management.dto.VehicleUsageUpdateRequestDto;

import java.util.List;

public interface VehicleService {

    VehicleResponseDto createVehicle(VehicleRequestDto requestDto);

    List<VehicleResponseDto> getAllVehicles();

    VehicleResponseDto getVehicleById(Long id);

    VehicleResponseDto updateVehicle(Long id, VehicleRequestDto requestDto);

    VehicleResponseDto assignGroup(Long vehicleId, Long groupId);

    VehicleResponseDto removeGroup(Long vehicleId);

    VehicleResponseDto updateUsage(Long vehicleId, VehicleUsageUpdateRequestDto requestDto);

    void deleteVehicle(Long id);
}
