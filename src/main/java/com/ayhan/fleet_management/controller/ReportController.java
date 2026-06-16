package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.DashboardOverviewResponseDto;
import com.ayhan.fleet_management.dto.LowStockSummaryResponseDto;
import com.ayhan.fleet_management.dto.MaintenanceStatusSummaryResponseDto;
import com.ayhan.fleet_management.dto.PartConsumptionReportDto;
import com.ayhan.fleet_management.dto.RecentCompletedWorkOrderResponseDto;
import com.ayhan.fleet_management.dto.VehicleMaintenanceCostReportDto;
import com.ayhan.fleet_management.dto.WorkOrderStatusSummaryResponseDto;
import com.ayhan.fleet_management.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard-summary")
    public ResponseEntity<DashboardOverviewResponseDto> getDashboardSummary() {
        return ResponseEntity.ok(reportService.getDashboardSummary());
    }

    @GetMapping("/work-orders/status-summary")
    public ResponseEntity<WorkOrderStatusSummaryResponseDto> getWorkOrderStatusSummary() {
        return ResponseEntity.ok(reportService.getWorkOrderStatusSummary());
    }

    @GetMapping("/work-orders/recent-completed")
    public ResponseEntity<List<RecentCompletedWorkOrderResponseDto>> getRecentCompletedWorkOrders() {
        return ResponseEntity.ok(reportService.getRecentCompletedWorkOrders());
    }

    @GetMapping("/maintenance/status-summary")
    public ResponseEntity<MaintenanceStatusSummaryResponseDto> getMaintenanceStatusSummary() {
        return ResponseEntity.ok(reportService.getMaintenanceStatusSummary());
    }

    @GetMapping("/vehicles/maintenance-costs")
    public ResponseEntity<List<VehicleMaintenanceCostReportDto>> getVehicleMaintenanceCosts() {
        return ResponseEntity.ok(reportService.getVehicleMaintenanceCosts());
    }

    @GetMapping("/parts/consumption")
    public ResponseEntity<List<PartConsumptionReportDto>> getPartConsumptionReport() {
        return ResponseEntity.ok(reportService.getPartConsumptionReport());
    }

    @GetMapping("/inventory/low-stock-summary")
    public ResponseEntity<LowStockSummaryResponseDto> getLowStockSummary() {
        return ResponseEntity.ok(reportService.getLowStockSummary());
    }
}
