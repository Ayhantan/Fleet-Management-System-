package com.ayhan.fleet_management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderCompletionRequestDto {

    @NotBlank(message = "Completion notes are required")
    private String completionNotes;

    @DecimalMin(value = "0.0", inclusive = true, message = "Actual cost must be greater than or equal to 0")
    private BigDecimal actualCost;

    @DecimalMin(value = "0.0", inclusive = true, message = "Labor hours must be greater than or equal to 0")
    private BigDecimal laborHours;
}
