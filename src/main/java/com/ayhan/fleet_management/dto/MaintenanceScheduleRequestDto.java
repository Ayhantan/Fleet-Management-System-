package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.entity.enums.TimeIntervalUnit;
import jakarta.validation.constraints.Min;
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
public class MaintenanceScheduleRequestDto {

    @NotNull(message = "Vehicle id is required")
    private Long vehicleId;

    @NotNull(message = "Maintenance definition id is required")
    private Long maintenanceDefinitionId;

    @NotNull(message = "Trigger type is required")
    private MaintenanceTriggerType triggerType;

    @Min(value = 1, message = "Hour interval must be greater than 0")
    private Integer intervalHour;

    @Min(value = 1, message = "Distance interval must be greater than 0")
    private Integer intervalDistance;

    @Min(value = 1, message = "Time interval value must be greater than 0")
    private Integer intervalTimeValue;

    private TimeIntervalUnit intervalTimeUnit;

    private Boolean active;
}
