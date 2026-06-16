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
public class VehicleMaintenanceCostReportDto {

    private Long vehicleId;
    private String vehicleDisplayName;
    private BigDecimal partCostTotal;
    private BigDecimal laborCostTotal;
    private BigDecimal externalServiceCostTotal;
    private BigDecimal miscCostTotal;
    private BigDecimal grandTotal;
}
