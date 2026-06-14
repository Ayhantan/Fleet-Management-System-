package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceTaskRequestDto {

    @NotNull(message = "Vehicle id is required")
    private Long vehicleId;

    private Long maintenanceScheduleId;

    private Long maintenanceDefinitionId;

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private MaintenancePriority priority;

    private LocalDate plannedDate;

    private LocalDate dueDate;

    private Integer dueHourMeter;

    private Integer dueDistanceReading;

    private String notes;
}
