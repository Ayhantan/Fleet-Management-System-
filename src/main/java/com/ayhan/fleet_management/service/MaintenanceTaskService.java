package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.MaintenanceTaskCompletionRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceTaskRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceTaskResponseDto;

import java.util.List;

public interface MaintenanceTaskService {

    MaintenanceTaskResponseDto createMaintenanceTask(MaintenanceTaskRequestDto requestDto);

    List<MaintenanceTaskResponseDto> getAllMaintenanceTasks();

    MaintenanceTaskResponseDto getMaintenanceTaskById(Long id);

    MaintenanceTaskResponseDto updateMaintenanceTask(Long id, MaintenanceTaskRequestDto requestDto);

    MaintenanceTaskResponseDto completeMaintenanceTask(Long id, MaintenanceTaskCompletionRequestDto requestDto);

    List<MaintenanceTaskResponseDto> getVehicleMaintenanceHistory(Long vehicleId);

    void deleteMaintenanceTask(Long id);
}
