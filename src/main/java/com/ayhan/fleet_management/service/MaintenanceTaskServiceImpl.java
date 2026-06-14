package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.MaintenanceTaskCompletionRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceTaskRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceTaskResponseDto;
import com.ayhan.fleet_management.entity.MaintenanceDefinition;
import com.ayhan.fleet_management.entity.MaintenanceSchedule;
import com.ayhan.fleet_management.entity.MaintenanceTask;
import com.ayhan.fleet_management.entity.Vehicle;
import com.ayhan.fleet_management.entity.enums.MaintenanceTaskStatus;
import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.MaintenanceDefinitionRepository;
import com.ayhan.fleet_management.repository.MaintenanceScheduleRepository;
import com.ayhan.fleet_management.repository.MaintenanceTaskRepository;
import com.ayhan.fleet_management.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceTaskServiceImpl implements MaintenanceTaskService {

    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final MaintenanceDefinitionRepository maintenanceDefinitionRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceCalculationService maintenanceCalculationService;

    @Override
    public MaintenanceTaskResponseDto createMaintenanceTask(MaintenanceTaskRequestDto requestDto) {
        Vehicle vehicle = findVehicleById(requestDto.getVehicleId());
        MaintenanceSchedule maintenanceSchedule = resolveSchedule(requestDto.getMaintenanceScheduleId());
        MaintenanceDefinition maintenanceDefinition = resolveDefinition(requestDto.getMaintenanceDefinitionId());

        if (maintenanceDefinition == null && maintenanceSchedule != null) {
            maintenanceDefinition = maintenanceSchedule.getMaintenanceDefinition();
        }

        MaintenanceTask maintenanceTask = MaintenanceTask.builder()
                .vehicle(vehicle)
                .maintenanceSchedule(maintenanceSchedule)
                .maintenanceDefinition(maintenanceDefinition)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .status(MaintenanceTaskStatus.PLANNED)
                .priority(requestDto.getPriority())
                .plannedDate(requestDto.getPlannedDate())
                .dueDate(requestDto.getDueDate())
                .dueHourMeter(requestDto.getDueHourMeter())
                .dueDistanceReading(requestDto.getDueDistanceReading())
                .notes(requestDto.getNotes())
                .build();

        return mapToResponseDto(maintenanceTaskRepository.save(maintenanceTask));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponseDto> getAllMaintenanceTasks() {
        return maintenanceTaskRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceTaskResponseDto getMaintenanceTaskById(Long id) {
        return mapToResponseDto(findMaintenanceTaskById(id));
    }

    @Override
    public MaintenanceTaskResponseDto updateMaintenanceTask(Long id, MaintenanceTaskRequestDto requestDto) {
        MaintenanceTask maintenanceTask = findMaintenanceTaskById(id);
        MaintenanceSchedule maintenanceSchedule = resolveSchedule(requestDto.getMaintenanceScheduleId());
        MaintenanceDefinition maintenanceDefinition = resolveDefinition(requestDto.getMaintenanceDefinitionId());

        if (maintenanceDefinition == null && maintenanceSchedule != null) {
            maintenanceDefinition = maintenanceSchedule.getMaintenanceDefinition();
        }

        maintenanceTask.setVehicle(findVehicleById(requestDto.getVehicleId()));
        maintenanceTask.setMaintenanceSchedule(maintenanceSchedule);
        maintenanceTask.setMaintenanceDefinition(maintenanceDefinition);
        maintenanceTask.setTitle(requestDto.getTitle());
        maintenanceTask.setDescription(requestDto.getDescription());
        maintenanceTask.setPriority(requestDto.getPriority());
        maintenanceTask.setPlannedDate(requestDto.getPlannedDate());
        maintenanceTask.setDueDate(requestDto.getDueDate());
        maintenanceTask.setDueHourMeter(requestDto.getDueHourMeter());
        maintenanceTask.setDueDistanceReading(requestDto.getDueDistanceReading());
        maintenanceTask.setNotes(requestDto.getNotes());

        return mapToResponseDto(maintenanceTaskRepository.save(maintenanceTask));
    }

    @Override
    public MaintenanceTaskResponseDto completeMaintenanceTask(Long id, MaintenanceTaskCompletionRequestDto requestDto) {
        MaintenanceTask maintenanceTask = findMaintenanceTaskById(id);
        maintenanceTask.setStatus(MaintenanceTaskStatus.COMPLETED);
        maintenanceTask.setCompletedDate(requestDto.getCompletedDate() != null ? requestDto.getCompletedDate() : LocalDate.now());
        maintenanceTask.setCompletedHourMeter(requestDto.getCompletedHourMeter());
        maintenanceTask.setCompletedDistanceReading(requestDto.getCompletedDistanceReading());
        maintenanceTask.setNotes(requestDto.getNotes());

        Vehicle vehicle = maintenanceTask.getVehicle();
        if (maintenanceTask.getCompletedDate() != null) {
            vehicle.setLastMaintenanceDate(maintenanceTask.getCompletedDate());
        }

        if (maintenanceTask.getCompletedHourMeter() != null) {
            if (vehicle.getCurrentHourMeter() == null || maintenanceTask.getCompletedHourMeter() > vehicle.getCurrentHourMeter()) {
                vehicle.setCurrentHourMeter(maintenanceTask.getCompletedHourMeter());
            }
        }

        if (maintenanceTask.getCompletedDistanceReading() != null) {
            if (vehicle.getCurrentDistanceReading() == null
                    || maintenanceTask.getCompletedDistanceReading() > vehicle.getCurrentDistanceReading()) {
                vehicle.setCurrentDistanceReading(maintenanceTask.getCompletedDistanceReading());
            }
        }

        MaintenanceSchedule maintenanceSchedule = maintenanceTask.getMaintenanceSchedule();
        if (maintenanceSchedule != null) {
            updateVehicleCompatibilityFields(vehicle, maintenanceSchedule.getTriggerType(), maintenanceTask);
            maintenanceCalculationService.recalculateSchedule(maintenanceSchedule);
            maintenanceScheduleRepository.save(maintenanceSchedule);
        }

        vehicleRepository.save(vehicle);
        return mapToResponseDto(maintenanceTaskRepository.save(maintenanceTask));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponseDto> getVehicleMaintenanceHistory(Long vehicleId) {
        return maintenanceTaskRepository.findByVehicleIdAndStatusOrderByCompletedDateDescCreatedAtDesc(
                        vehicleId,
                        MaintenanceTaskStatus.COMPLETED
                )
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public void deleteMaintenanceTask(Long id) {
        maintenanceTaskRepository.delete(findMaintenanceTaskById(id));
    }

    private void updateVehicleCompatibilityFields(
            Vehicle vehicle,
            MaintenanceTriggerType triggerType,
            MaintenanceTask maintenanceTask
    ) {
        if (requiresHours(triggerType) && maintenanceTask.getCompletedHourMeter() != null) {
            vehicle.setLastMaintenanceHourMeter(maintenanceTask.getCompletedHourMeter());
        }

        if (requiresDistance(triggerType) && maintenanceTask.getCompletedDistanceReading() != null) {
            vehicle.setLastMaintenanceDistanceReading(maintenanceTask.getCompletedDistanceReading());
        }
    }

    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    private MaintenanceSchedule findMaintenanceScheduleById(Long id) {
        return maintenanceScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance schedule not found with id: " + id));
    }

    private MaintenanceDefinition findMaintenanceDefinitionById(Long id) {
        return maintenanceDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance definition not found with id: " + id));
    }

    private MaintenanceTask findMaintenanceTaskById(Long id) {
        return maintenanceTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance task not found with id: " + id));
    }

    private MaintenanceSchedule resolveSchedule(Long scheduleId) {
        return scheduleId != null ? findMaintenanceScheduleById(scheduleId) : null;
    }

    private MaintenanceDefinition resolveDefinition(Long definitionId) {
        return definitionId != null ? findMaintenanceDefinitionById(definitionId) : null;
    }

    private boolean requiresHours(MaintenanceTriggerType triggerType) {
        return triggerType == MaintenanceTriggerType.HOURS
                || triggerType == MaintenanceTriggerType.TIME_AND_HOURS;
    }

    private boolean requiresDistance(MaintenanceTriggerType triggerType) {
        return triggerType == MaintenanceTriggerType.DISTANCE
                || triggerType == MaintenanceTriggerType.TIME_AND_DISTANCE;
    }

    private MaintenanceTaskResponseDto mapToResponseDto(MaintenanceTask maintenanceTask) {
        return MaintenanceTaskResponseDto.builder()
                .id(maintenanceTask.getId())
                .vehicleId(maintenanceTask.getVehicle().getId())
                .vehicleName(maintenanceTask.getVehicle().getName())
                .maintenanceScheduleId(
                        maintenanceTask.getMaintenanceSchedule() != null ? maintenanceTask.getMaintenanceSchedule().getId() : null
                )
                .maintenanceDefinitionId(
                        maintenanceTask.getMaintenanceDefinition() != null ? maintenanceTask.getMaintenanceDefinition().getId() : null
                )
                .maintenanceDefinitionName(
                        maintenanceTask.getMaintenanceDefinition() != null ? maintenanceTask.getMaintenanceDefinition().getName() : null
                )
                .title(maintenanceTask.getTitle())
                .description(maintenanceTask.getDescription())
                .status(maintenanceTask.getStatus())
                .priority(maintenanceTask.getPriority())
                .plannedDate(maintenanceTask.getPlannedDate())
                .dueDate(maintenanceTask.getDueDate())
                .dueHourMeter(maintenanceTask.getDueHourMeter())
                .dueDistanceReading(maintenanceTask.getDueDistanceReading())
                .completedDate(maintenanceTask.getCompletedDate())
                .completedHourMeter(maintenanceTask.getCompletedHourMeter())
                .completedDistanceReading(maintenanceTask.getCompletedDistanceReading())
                .notes(maintenanceTask.getNotes())
                .createdAt(maintenanceTask.getCreatedAt())
                .updatedAt(maintenanceTask.getUpdatedAt())
                .build();
    }
}
