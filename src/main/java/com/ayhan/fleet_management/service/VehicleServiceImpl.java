package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.VehicleRequestDto;
import com.ayhan.fleet_management.dto.VehicleResponseDto;
import com.ayhan.fleet_management.dto.VehicleUsageUpdateRequestDto;
import com.ayhan.fleet_management.entity.Vehicle;
import com.ayhan.fleet_management.entity.VehicleGroup;
import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.exception.DuplicateResourceException;
import com.ayhan.fleet_management.exception.InvalidMaintenanceConfigurationException;
import com.ayhan.fleet_management.exception.InvalidVehicleConfigurationException;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.VehicleGroupRepository;
import com.ayhan.fleet_management.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleGroupRepository vehicleGroupRepository;
    private final MaintenanceScheduleService maintenanceScheduleService;

    @Override
    public VehicleResponseDto createVehicle(VehicleRequestDto requestDto) {
        validateUniquePlateNumber(requestDto.getPlateNumber(), null);
        validateMaintenanceConfiguration(requestDto);
        Vehicle vehicle = mapToEntity(requestDto);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapToResponseDto(savedVehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponseDto> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleById(Long id) {
        Vehicle vehicle = findVehicleById(id);
        return mapToResponseDto(vehicle);
    }

    @Override
    public VehicleResponseDto updateVehicle(Long id, VehicleRequestDto requestDto) {
        validateUniquePlateNumber(requestDto.getPlateNumber(), id);
        validateMaintenanceConfiguration(requestDto);

        Vehicle existingVehicle = findVehicleById(id);
        existingVehicle.setName(requestDto.getName());
        existingVehicle.setPlateNumber(requestDto.getPlateNumber());
        existingVehicle.setBrand(requestDto.getBrand());
        existingVehicle.setModel(requestDto.getModel());
        existingVehicle.setModelYear(requestDto.getModelYear());
        existingVehicle.setType(requestDto.getType());
        existingVehicle.setStatus(requestDto.getStatus());
        existingVehicle.setImageUrl(requestDto.getImageUrl());
        existingVehicle.setVehicleGroup(resolveVehicleGroup(requestDto.getVehicleGroupId()));
        existingVehicle.setCategory(requestDto.getCategory());
        existingVehicle.setCurrentHourMeter(requestDto.getCurrentHourMeter());
        existingVehicle.setCurrentDistanceReading(requestDto.getCurrentDistanceReading());
        existingVehicle.setLastMaintenanceDate(requestDto.getLastMaintenanceDate());
        existingVehicle.setLastMaintenanceHourMeter(requestDto.getLastMaintenanceHourMeter());
        existingVehicle.setLastMaintenanceDistanceReading(requestDto.getLastMaintenanceDistanceReading());
        existingVehicle.setMaintenanceTriggerType(requestDto.getMaintenanceTriggerType());
        existingVehicle.setHourIntervalValue(requestDto.getHourIntervalValue());
        existingVehicle.setDistanceIntervalValue(requestDto.getDistanceIntervalValue());
        existingVehicle.setTimeIntervalValue(requestDto.getTimeIntervalValue());
        existingVehicle.setTimeIntervalUnit(requestDto.getTimeIntervalUnit());
        existingVehicle.setDistanceUnit(requestDto.getDistanceUnit());

        Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
        return mapToResponseDto(updatedVehicle);
    }

    @Override
    public VehicleResponseDto assignGroup(Long vehicleId, Long groupId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        vehicle.setVehicleGroup(findVehicleGroupById(groupId));
        return mapToResponseDto(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponseDto removeGroup(Long vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        vehicle.setVehicleGroup(null);
        return mapToResponseDto(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponseDto updateUsage(Long vehicleId, VehicleUsageUpdateRequestDto requestDto) {
        if (requestDto.getCurrentHourMeter() == null && requestDto.getCurrentDistanceReading() == null) {
            throw new InvalidMaintenanceConfigurationException(
                    "At least one usage field must be provided"
            );
        }

        Vehicle vehicle = findVehicleById(vehicleId);

        if (requestDto.getCurrentHourMeter() != null) {
            vehicle.setCurrentHourMeter(requestDto.getCurrentHourMeter());
        }

        if (requestDto.getCurrentDistanceReading() != null) {
            vehicle.setCurrentDistanceReading(requestDto.getCurrentDistanceReading());
        }

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        maintenanceScheduleService.recalculateActiveSchedulesForVehicle(vehicleId);
        return mapToResponseDto(updatedVehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = findVehicleById(id);
        vehicleRepository.delete(vehicle);
    }

    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    private VehicleGroup findVehicleGroupById(Long id) {
        return vehicleGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle group not found with id: " + id));
    }

    private VehicleGroup resolveVehicleGroup(Long vehicleGroupId) {
        if (vehicleGroupId == null) {
            return null;
        }
        return findVehicleGroupById(vehicleGroupId);
    }

    private void validateUniquePlateNumber(String plateNumber, Long currentVehicleId) {
        boolean exists = vehicleRepository.findAll()
                .stream()
                .anyMatch(vehicle -> vehicle.getPlateNumber().equalsIgnoreCase(plateNumber)
                        && (currentVehicleId == null || !vehicle.getId().equals(currentVehicleId)));

        if (exists) {
            throw new DuplicateResourceException("Plate number is already in use");
        }
    }

    private void validateMaintenanceConfiguration(VehicleRequestDto requestDto) {
        MaintenanceTriggerType triggerType = requestDto.getMaintenanceTriggerType();

        if (requiresHours(triggerType) && requestDto.getHourIntervalValue() == null) {
            throw new InvalidVehicleConfigurationException(
                    "Hour interval value is required when maintenance trigger type includes HOURS"
            );
        }

        if (requiresDistance(triggerType) && requestDto.getDistanceIntervalValue() == null) {
            throw new InvalidVehicleConfigurationException(
                    "Distance interval value is required when maintenance trigger type includes DISTANCE"
            );
        }

        if (requiresHours(triggerType) && requestDto.getCurrentHourMeter() == null) {
            throw new InvalidVehicleConfigurationException(
                    "Current hour meter is required when maintenance trigger type includes HOURS"
            );
        }

        if (requiresDistance(triggerType) && requestDto.getCurrentDistanceReading() == null) {
            throw new InvalidVehicleConfigurationException(
                    "Current distance reading is required when maintenance trigger type includes DISTANCE"
            );
        }

        if (requiresDistance(triggerType) && requestDto.getDistanceUnit() == null) {
            throw new InvalidVehicleConfigurationException(
                    "Distance unit is required when maintenance trigger type includes DISTANCE"
            );
        }

        if (requiresTime(triggerType) && requestDto.getTimeIntervalValue() == null) {
            throw new InvalidVehicleConfigurationException(
                    "Time interval value is required when maintenance trigger type includes TIME"
            );
        }

        if (requiresTime(triggerType) && requestDto.getTimeIntervalUnit() == null) {
            throw new InvalidVehicleConfigurationException(
                    "Time interval unit is required when maintenance trigger type includes TIME"
            );
        }

        if (requestDto.getLastMaintenanceHourMeter() != null
                && requestDto.getCurrentHourMeter() != null
                && requestDto.getLastMaintenanceHourMeter() > requestDto.getCurrentHourMeter()) {
            throw new InvalidVehicleConfigurationException(
                    "Last maintenance hour meter cannot be greater than current hour meter"
            );
        }

        if (requestDto.getLastMaintenanceDistanceReading() != null
                && requestDto.getCurrentDistanceReading() != null
                && requestDto.getLastMaintenanceDistanceReading() > requestDto.getCurrentDistanceReading()) {
            throw new InvalidVehicleConfigurationException(
                    "Last maintenance distance reading cannot be greater than current distance reading"
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

    private Vehicle mapToEntity(VehicleRequestDto requestDto) {
        return Vehicle.builder()
                .name(requestDto.getName())
                .plateNumber(requestDto.getPlateNumber())
                .brand(requestDto.getBrand())
                .model(requestDto.getModel())
                .modelYear(requestDto.getModelYear())
                .type(requestDto.getType())
                .status(requestDto.getStatus())
                .imageUrl(requestDto.getImageUrl())
                .vehicleGroup(resolveVehicleGroup(requestDto.getVehicleGroupId()))
                .category(requestDto.getCategory())
                .currentHourMeter(requestDto.getCurrentHourMeter())
                .currentDistanceReading(requestDto.getCurrentDistanceReading())
                .lastMaintenanceDate(requestDto.getLastMaintenanceDate())
                .lastMaintenanceHourMeter(requestDto.getLastMaintenanceHourMeter())
                .lastMaintenanceDistanceReading(requestDto.getLastMaintenanceDistanceReading())
                .maintenanceTriggerType(requestDto.getMaintenanceTriggerType())
                .hourIntervalValue(requestDto.getHourIntervalValue())
                .distanceIntervalValue(requestDto.getDistanceIntervalValue())
                .timeIntervalValue(requestDto.getTimeIntervalValue())
                .timeIntervalUnit(requestDto.getTimeIntervalUnit())
                .distanceUnit(requestDto.getDistanceUnit())
                .build();
    }

    private VehicleResponseDto mapToResponseDto(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .name(vehicle.getName())
                .plateNumber(vehicle.getPlateNumber())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .modelYear(vehicle.getModelYear())
                .type(vehicle.getType())
                .status(vehicle.getStatus())
                .imageUrl(vehicle.getImageUrl())
                .vehicleGroupId(vehicle.getVehicleGroup() != null ? vehicle.getVehicleGroup().getId() : null)
                .vehicleGroupName(vehicle.getVehicleGroup() != null ? vehicle.getVehicleGroup().getName() : null)
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .category(vehicle.getCategory())
                .currentHourMeter(vehicle.getCurrentHourMeter())
                .currentDistanceReading(vehicle.getCurrentDistanceReading())
                .lastMaintenanceDate(vehicle.getLastMaintenanceDate())
                .lastMaintenanceHourMeter(vehicle.getLastMaintenanceHourMeter())
                .lastMaintenanceDistanceReading(vehicle.getLastMaintenanceDistanceReading())
                .maintenanceTriggerType(vehicle.getMaintenanceTriggerType())
                .hourIntervalValue(vehicle.getHourIntervalValue())
                .distanceIntervalValue(vehicle.getDistanceIntervalValue())
                .timeIntervalValue(vehicle.getTimeIntervalValue())
                .timeIntervalUnit(vehicle.getTimeIntervalUnit())
                .distanceUnit(vehicle.getDistanceUnit())
                .build();
    }
}
