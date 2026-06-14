package com.ayhan.fleet_management.dto;

import jakarta.validation.constraints.Min;
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
public class VehicleUsageUpdateRequestDto {

    @Min(value = 0, message = "Current hour meter must be greater than or equal to 0")
    private Integer currentHourMeter;

    @Min(value = 0, message = "Current distance reading must be greater than or equal to 0")
    private Integer currentDistanceReading;
}
