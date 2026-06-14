package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.MaintenanceScheduleRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceScheduleResponseDto;
import com.ayhan.fleet_management.entity.MaintenanceDefinition;
import com.ayhan.fleet_management.entity.MaintenanceSchedule;
import com.ayhan.fleet_management.entity.Vehicle;
import com.ayhan.fleet_management.entity.enums.CalculatedMaintenanceStatus;
import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.exception.InvalidMaintenanceConfigurationException;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.MaintenanceDefinitionRepository;
import com.ayhan.fleet_management.repository.MaintenanceScheduleRepository;
import com.ayhan.fleet_management.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceScheduleServiceImpl implements MaintenanceScheduleService {

    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final MaintenanceDefinitionRepository maintenanceDefinitionRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceCalculationService maintenanceCalculationService;

    @Override
    public MaintenanceScheduleResponseDto createMaintenanceSchedule(MaintenanceScheduleRequestDto requestDto) {
        validateScheduleConfiguration(requestDto);

        MaintenanceSchedule maintenanceSchedule = MaintenanceSchedule.builder()
                .vehicle(findVehicleById(requestDto.getVehicleId()))
                .maintenanceDefinition(findMaintenanceDefinitionById(requestDto.getMaintenanceDefinitionId()))
                .triggerType(requestDto.getTriggerType())
                .intervalHour(requestDto.getIntervalHour())
                .intervalDistance(requestDto.getIntervalDistance())
                .intervalTimeValue(requestDto.getIntervalTimeValue())
                .intervalTimeUnit(requestDto.getIntervalTimeUnit())
                .active(requestDto.getActive() != null ? requestDto.getActive() : Boolean.TRUE)
                .build();

        MaintenanceSchedule savedSchedule = maintenanceScheduleRepository.save(maintenanceSchedule);
        maintenanceCalculationService.recalculateSchedule(savedSchedule);
        return mapToResponseDto(maintenanceScheduleRepository.save(savedSchedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceScheduleResponseDto> getAllMaintenanceSchedules() {
        return maintenanceScheduleRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceScheduleResponseDto getMaintenanceScheduleById(Long id) {
        return mapToResponseDto(findMaintenanceScheduleById(id));
    }

    @Override
    public MaintenanceScheduleResponseDto updateMaintenanceSchedule(Long id, MaintenanceScheduleRequestDto requestDto) {
        validateScheduleConfiguration(requestDto);

        MaintenanceSchedule maintenanceSchedule = findMaintenanceScheduleById(id);
        maintenanceSchedule.setVehicle(findVehicleById(requestDto.getVehicleId()));
        maintenanceSchedule.setMaintenanceDefinition(findMaintenanceDefinitionById(requestDto.getMaintenanceDefinitionId()));
        maintenanceSchedule.setTriggerType(requestDto.getTriggerType());
        maintenanceSchedule.setIntervalHour(requestDto.getIntervalHour());
        maintenanceSchedule.setIntervalDistance(requestDto.getIntervalDistance());
        maintenanceSchedule.setIntervalTimeValue(requestDto.getIntervalTimeValue());
        maintenanceSchedule.setIntervalTimeUnit(requestDto.getIntervalTimeUnit());
        maintenanceSchedule.setActive(requestDto.getActive() != null ? requestDto.getActive() : maintenanceSchedule.getActive());

        maintenanceCalculationService.recalculateSchedule(maintenanceSchedule);
        return mapToResponseDto(maintenanceScheduleRepository.save(maintenanceSchedule));
    }

    @Override
    public void deleteMaintenanceSchedule(Long id) {
        maintenanceScheduleRepository.delete(findMaintenanceScheduleById(id));
    }

    @Override
    public MaintenanceScheduleResponseDto recalculateMaintenanceSchedule(Long id) {
        MaintenanceSchedule maintenanceSchedule = findMaintenanceScheduleById(id);
        maintenanceCalculationService.recalculateSchedule(maintenanceSchedule);
        return mapToResponseDto(maintenanceScheduleRepository.save(maintenanceSchedule));
    }

    @Override
    public void recalculateActiveSchedulesForVehicle(Long vehicleId) {
        List<MaintenanceSchedule> schedules = maintenanceScheduleRepository.findByVehicleIdAndActiveTrue(vehicleId);
        for (MaintenanceSchedule schedule : schedules) {
            maintenanceCalculationService.recalculateSchedule(schedule);
        }
        maintenanceScheduleRepository.saveAll(schedules);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceScheduleResponseDto> getUpcomingMaintenanceSchedules() {
        return maintenanceScheduleRepository.findByActiveTrue()
                .stream()
                .filter(schedule -> maintenanceCalculationService.calculateStatus(schedule.getVehicle(), schedule)
                        == CalculatedMaintenanceStatus.UPCOMING)
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceScheduleResponseDto> getOverdueMaintenanceSchedules() {
        return maintenanceScheduleRepository.findByActiveTrue()
                .stream()
                .filter(schedule -> maintenanceCalculationService.calculateStatus(schedule.getVehicle(), schedule)
                        == CalculatedMaintenanceStatus.OVERDUE)
                .map(this::mapToResponseDto)
                .toList();
    }

    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    private MaintenanceDefinition findMaintenanceDefinitionById(Long id) {
        return maintenanceDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance definition not found with id: " + id));
    }

    private MaintenanceSchedule findMaintenanceScheduleById(Long id) {
        return maintenanceScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance schedule not found with id: " + id));
    }

    private void validateScheduleConfiguration(MaintenanceScheduleRequestDto requestDto) {
        MaintenanceTriggerType triggerType = requestDto.getTriggerType();

        if (requiresHours(triggerType) && requestDto.getIntervalHour() == null) {
            throw new InvalidMaintenanceConfigurationException(
                    "Hour interval is required when maintenance trigger type includes HOURS"
            );
        }

        if (requiresDistance(triggerType) && requestDto.getIntervalDistance() == null) {
            throw new InvalidMaintenanceConfigurationException(
                    "Distance interval is required when maintenance trigger type includes DISTANCE"
            );
        }

        if (requiresTime(triggerType) && requestDto.getIntervalTimeValue() == null) {
            throw new InvalidMaintenanceConfigurationException(
                    "Time interval value is required when maintenance trigger type includes TIME"
            );
        }

        if (requiresTime(triggerType) && requestDto.getIntervalTimeUnit() == null) {
            throw new InvalidMaintenanceConfigurationException(
                    "Time interval unit is required when maintenance trigger type includes TIME"
            );
        }
    }

    private boolean requiresHours(MaintenanceTriggerType triggerType) {
        return triggerType == MaintenanceTriggerType.HOURS
                || triggerType == MaintenanceTriggerType.TIME_AND_HOURS;
    }

    private boolean requiresDistance(MaintenanceTriggerType triggerType) {
        return triggerType == MaintenanceTriggerType.DISTANCE
                || triggerType == MaintenanceTriggerType.TIME_AND_DISTANCE;
    }

    private boolean requiresTime(MaintenanceTriggerType triggerType) {
        return triggerType == MaintenanceTriggerType.TIME
                || triggerType == MaintenanceTriggerType.TIME_AND_HOURS
                || triggerType == MaintenanceTriggerType.TIME_AND_DISTANCE;
    }

    private MaintenanceScheduleResponseDto mapToResponseDto(MaintenanceSchedule maintenanceSchedule) {
        return MaintenanceScheduleResponseDto.builder()
                .id(maintenanceSchedule.getId())
                .vehicleId(maintenanceSchedule.getVehicle().getId())
                .vehicleName(maintenanceSchedule.getVehicle().getName())
                .maintenanceDefinitionId(maintenanceSchedule.getMaintenanceDefinition().getId())
                .maintenanceDefinitionName(maintenanceSchedule.getMaintenanceDefinition().getName())
                .maintenanceDefinitionCategory(maintenanceSchedule.getMaintenanceDefinition().getCategory())
                .triggerType(maintenanceSchedule.getTriggerType())
                .intervalHour(maintenanceSchedule.getIntervalHour())
                .intervalDistance(maintenanceSchedule.getIntervalDistance())
                .intervalTimeValue(maintenanceSchedule.getIntervalTimeValue())
                .intervalTimeUnit(maintenanceSchedule.getIntervalTimeUnit())
                .nextDueDate(maintenanceSchedule.getNextDueDate())
                .nextDueHourMeter(maintenanceSchedule.getNextDueHourMeter())
                .nextDueDistanceReading(maintenanceSchedule.getNextDueDistanceReading())
                .active(maintenanceSchedule.getActive())
                .calculatedStatus(maintenanceCalculationService.calculateStatus(
                        maintenanceSchedule.getVehicle(),
                        maintenanceSchedule
                ))
                .createdAt(maintenanceSchedule.getCreatedAt())
                .updatedAt(maintenanceSchedule.getUpdatedAt())
                .build();
    }
}
