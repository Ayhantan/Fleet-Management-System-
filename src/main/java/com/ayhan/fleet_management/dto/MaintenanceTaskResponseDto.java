package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.MaintenancePriority;
import com.ayhan.fleet_management.entity.enums.MaintenanceTaskStatus;
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
public class MaintenanceTaskResponseDto {

    private Long id;
    private Long vehicleId;
    private String vehicleName;
    private Long maintenanceScheduleId;
    private Long maintenanceDefinitionId;
    private String maintenanceDefinitionName;
    private String title;
    private String description;
    private MaintenanceTaskStatus status;
    private MaintenancePriority priority;
    private LocalDate plannedDate;
    private LocalDate dueDate;
    private Integer dueHourMeter;
    private Integer dueDistanceReading;
    private LocalDate completedDate;
    private Integer completedHourMeter;
    private Integer completedDistanceReading;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
