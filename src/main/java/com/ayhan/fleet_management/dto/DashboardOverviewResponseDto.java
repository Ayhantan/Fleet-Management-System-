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
public class DashboardOverviewResponseDto {

    private long totalVehicles;
    private long totalWorkOrders;
    private long openWorkOrders;
    private long assignedWorkOrders;
    private long inProgressWorkOrders;
    private long completedWorkOrders;
    private long cancelledWorkOrders;
    private long upcomingMaintenanceCount;
    private long overdueMaintenanceCount;
    private long lowStockItemCount;
    private BigDecimal partCostTotal;
    private BigDecimal laborCostTotal;
    private BigDecimal externalServiceCostTotal;
    private BigDecimal miscCostTotal;
    private BigDecimal grandTotalMaintenanceCost;
}
