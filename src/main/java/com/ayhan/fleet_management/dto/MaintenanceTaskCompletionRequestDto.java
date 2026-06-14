package com.ayhan.fleet_management.dto;

import jakarta.validation.constraints.Min;
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
public class MaintenanceTaskCompletionRequestDto {

    private LocalDate completedDate;

    @Min(value = 0, message = "Completed hour meter must be greater than or equal to 0")
    private Integer completedHourMeter;

    @Min(value = 0, message = "Completed distance reading must be greater than or equal to 0")
    private Integer completedDistanceReading;

    private String notes;
}
