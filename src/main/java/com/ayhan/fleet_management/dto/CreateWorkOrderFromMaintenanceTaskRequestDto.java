package com.ayhan.fleet_management.dto;

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
public class CreateWorkOrderFromMaintenanceTaskRequestDto {

    @NotNull(message = "Maintenance task id is required")
    private Long maintenanceTaskId;

    private Long assignedUserId;

    private String title;

    private String description;
}
