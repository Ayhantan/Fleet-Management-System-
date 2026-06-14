package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.DistanceUnit;
import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.entity.enums.TimeIntervalUnit;
import com.ayhan.fleet_management.entity.enums.VehicleStatus;
import jakarta.validation.constraints.Min;
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
public class VehicleRequestDto {

    @NotBlank(message = "Vehicle name is required")
    private String name;

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Model year is required")
    @Min(value = 1900, message = "Model year must be valid")
    private Integer modelYear;

    @NotBlank(message = "Vehicle type is required")
    private String type;

    @NotNull(message = "Vehicle status is required")
    private VehicleStatus status;

    private String imageUrl;

    private Long vehicleGroupId;

    @NotBlank(message = "Vehicle category is required")
    private String category;

    @Min(value = 0, message = "Current hour meter must be greater than or equal to 0")
    private Integer currentHourMeter;

    @Min(value = 0, message = "Current distance reading must be greater than or equal to 0")
    private Integer currentDistanceReading;

    private LocalDate lastMaintenanceDate;

    @Min(value = 0, message = "Last maintenance hour meter must be greater than or equal to 0")
    private Integer lastMaintenanceHourMeter;

    @Min(value = 0, message = "Last maintenance distance reading must be greater than or equal to 0")
    private Integer lastMaintenanceDistanceReading;

    @NotNull(message = "Maintenance trigger type is required")
    private MaintenanceTriggerType maintenanceTriggerType;

    @Min(value = 1, message = "Hour interval value must be greater than 0")
    private Integer hourIntervalValue;

    @Min(value = 1, message = "Distance interval value must be greater than 0")
    private Integer distanceIntervalValue;

    @Min(value = 1, message = "Time interval value must be greater than 0")
    private Integer timeIntervalValue;

    private TimeIntervalUnit timeIntervalUnit;

    private DistanceUnit distanceUnit;
}
