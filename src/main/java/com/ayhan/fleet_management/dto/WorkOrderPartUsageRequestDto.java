package com.ayhan.fleet_management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class WorkOrderPartUsageRequestDto {

    @NotNull(message = "Part id is required")
    private Long partId;

    @NotNull(message = "Quantity used is required")
    @Min(value = 1, message = "Quantity used must be greater than 0")
    private Integer quantityUsed;

    private BigDecimal unitCost;

    private String notes;
}
