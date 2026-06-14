package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.VehicleGroupRequestDto;
import com.ayhan.fleet_management.dto.VehicleGroupResponseDto;

import java.util.List;

public interface VehicleGroupService {

    VehicleGroupResponseDto createVehicleGroup(VehicleGroupRequestDto requestDto);

    List<VehicleGroupResponseDto> getAllVehicleGroups();

    VehicleGroupResponseDto getVehicleGroupById(Long id);

    VehicleGroupResponseDto updateVehicleGroup(Long id, VehicleGroupRequestDto requestDto);

    void deleteVehicleGroup(Long id);
}
