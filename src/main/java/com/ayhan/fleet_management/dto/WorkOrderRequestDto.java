package com.ayhan.fleet_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderRequestDto {

    @NotNull(message = "Vehicle id is required")
    private Long vehicleId;

    private Long maintenanceTaskId;

    private Long assignedUserId;

    @NotBlank(message = "Work order title is required")
    private String title;

    private String description;
}
