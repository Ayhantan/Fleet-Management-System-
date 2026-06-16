package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.DashboardOverviewResponseDto;
import com.ayhan.fleet_management.dto.LowStockSummaryResponseDto;
import com.ayhan.fleet_management.dto.MaintenanceStatusSummaryResponseDto;
import com.ayhan.fleet_management.dto.PartConsumptionReportDto;
import com.ayhan.fleet_management.dto.RecentCompletedWorkOrderResponseDto;
import com.ayhan.fleet_management.dto.VehicleMaintenanceCostReportDto;
import com.ayhan.fleet_management.dto.WorkOrderStatusSummaryResponseDto;

import java.util.List;

public interface ReportService {

    DashboardOverviewResponseDto getDashboardSummary();

    WorkOrderStatusSummaryResponseDto getWorkOrderStatusSummary();

    List<RecentCompletedWorkOrderResponseDto> getRecentCompletedWorkOrders();

    MaintenanceStatusSummaryResponseDto getMaintenanceStatusSummary();

    List<VehicleMaintenanceCostReportDto> getVehicleMaintenanceCosts();

    List<PartConsumptionReportDto> getPartConsumptionReport();

    LowStockSummaryResponseDto getLowStockSummary();
}
