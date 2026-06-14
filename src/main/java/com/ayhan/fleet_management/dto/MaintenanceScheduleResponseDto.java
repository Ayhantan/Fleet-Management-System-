package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.CalculatedMaintenanceStatus;
import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.entity.enums.TimeIntervalUnit;
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
public class MaintenanceScheduleResponseDto {

    private Long id;
    private Long vehicleId;
    private String vehicleName;
    private Long maintenanceDefinitionId;
    private String maintenanceDefinitionName;
    private String maintenanceDefinitionCategory;
    private MaintenanceTriggerType triggerType;
    private Integer intervalHour;
    private Integer intervalDistance;
    private Integer intervalTimeValue;
    private TimeIntervalUnit intervalTimeUnit;
    private LocalDate nextDueDate;
    private Integer nextDueHourMeter;
    private Integer nextDueDistanceReading;
    private Boolean active;
    private CalculatedMaintenanceStatus calculatedStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
