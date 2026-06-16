package com.ayhan.fleet_management.dto;

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
public class PartConsumptionReportDto {

    private Long partId;
    private String partNumber;
    private String partName;
    private Integer totalQuantityUsed;
    private BigDecimal totalCost;
}
