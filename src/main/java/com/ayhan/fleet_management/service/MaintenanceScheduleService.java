package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.MaintenanceScheduleRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceScheduleResponseDto;

import java.util.List;

public interface MaintenanceScheduleService {

    MaintenanceScheduleResponseDto createMaintenanceSchedule(MaintenanceScheduleRequestDto requestDto);

    List<MaintenanceScheduleResponseDto> getAllMaintenanceSchedules();

    MaintenanceScheduleResponseDto getMaintenanceScheduleById(Long id);

    MaintenanceScheduleResponseDto updateMaintenanceSchedule(Long id, MaintenanceScheduleRequestDto requestDto);

    void deleteMaintenanceSchedule(Long id);

    MaintenanceScheduleResponseDto recalculateMaintenanceSchedule(Long id);

    void recalculateActiveSchedulesForVehicle(Long vehicleId);

    List<MaintenanceScheduleResponseDto> getUpcomingMaintenanceSchedules();

    List<MaintenanceScheduleResponseDto> getOverdueMaintenanceSchedules();
}
