package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.DistanceUnit;
import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.entity.enums.TimeIntervalUnit;
import com.ayhan.fleet_management.entity.enums.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponseDto {

    private Long id;
    private String name;
    private String plateNumber;
    private String brand;
    private String model;
    private Integer modelYear;
    private String type;
    private VehicleStatus status;
    private String imageUrl;
    private Long vehicleGroupId;
    private String vehicleGroupName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;
    private Integer currentHourMeter;
    private Integer currentDistanceReading;
    private LocalDate lastMaintenanceDate;
    private Integer lastMaintenanceHourMeter;
    private Integer lastMaintenanceDistanceReading;
    private MaintenanceTriggerType maintenanceTriggerType;
    private Integer hourIntervalValue;
    private Integer distanceIntervalValue;
    private Integer timeIntervalValue;
    private TimeIntervalUnit timeIntervalUnit;
    private DistanceUnit distanceUnit;
}
