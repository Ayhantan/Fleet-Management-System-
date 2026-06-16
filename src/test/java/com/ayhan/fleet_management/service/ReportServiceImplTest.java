package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.DashboardOverviewResponseDto;
import com.ayhan.fleet_management.dto.LowStockSummaryResponseDto;
import com.ayhan.fleet_management.dto.MaintenanceStatusSummaryResponseDto;
import com.ayhan.fleet_management.dto.PartConsumptionReportDto;
import com.ayhan.fleet_management.dto.RecentCompletedWorkOrderResponseDto;
import com.ayhan.fleet_management.dto.VehicleMaintenanceCostReportDto;
import com.ayhan.fleet_management.dto.WorkOrderStatusSummaryResponseDto;
import com.ayhan.fleet_management.entity.InventoryItem;
import com.ayhan.fleet_management.entity.MaintenanceSchedule;
import com.ayhan.fleet_management.entity.Part;
import com.ayhan.fleet_management.entity.Vehicle;
import com.ayhan.fleet_management.entity.WorkOrder;
import com.ayhan.fleet_management.entity.WorkOrderExpense;
import com.ayhan.fleet_management.entity.WorkOrderPartUsage;
import com.ayhan.fleet_management.entity.enums.CalculatedMaintenanceStatus;
import com.ayhan.fleet_management.entity.enums.CostType;
import com.ayhan.fleet_management.entity.enums.WorkOrderStatus;
import com.ayhan.fleet_management.repository.InventoryItemRepository;
import com.ayhan.fleet_management.repository.MaintenanceScheduleRepository;
import com.ayhan.fleet_management.repository.VehicleRepository;
import com.ayhan.fleet_management.repository.WorkOrderExpenseRepository;
import com.ayhan.fleet_management.repository.WorkOrderPartUsageRepository;
import com.ayhan.fleet_management.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkOrderExpenseRepository workOrderExpenseRepository;

    @Mock
    private WorkOrderPartUsageRepository workOrderPartUsageRepository;

    @Mock
    private MaintenanceScheduleRepository maintenanceScheduleRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private MaintenanceCalculationService maintenanceCalculationService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void dashboardSummaryAggregatesCountsAndCostsWithoutUsingActualCost() {
        when(vehicleRepository.count()).thenReturn(3L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.OPEN)).thenReturn(2L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.ASSIGNED)).thenReturn(1L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.IN_PROGRESS)).thenReturn(1L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED)).thenReturn(4L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.CANCELLED)).thenReturn(1L);

        Vehicle vehicle = buildVehicle(1L, "34ABC123", "Ford", "Transit");
        WorkOrder workOrder = WorkOrder.builder()
                .id(50L)
                .vehicle(vehicle)
                .actualCost(new BigDecimal("9999.99"))
                .build();

        when(maintenanceScheduleRepository.findByActiveTrue()).thenReturn(List.of(
                MaintenanceSchedule.builder().id(1L).vehicle(vehicle).active(true).build(),
                MaintenanceSchedule.builder().id(2L).vehicle(vehicle).active(true).build()
        ));
        when(maintenanceCalculationService.calculateStatus(any(Vehicle.class), any(MaintenanceSchedule.class)))
                .thenReturn(CalculatedMaintenanceStatus.UPCOMING, CalculatedMaintenanceStatus.OVERDUE);

        when(inventoryItemRepository.findLowStockItems()).thenReturn(List.of(
                InventoryItem.builder()
                        .id(1L)
                        .part(Part.builder().id(2L).partNumber("P-01").name("Filter").build())
                        .currentQuantity(2)
                        .minimumStockLevel(5)
                        .location("A1")
                        .build()
        ));

        when(workOrderPartUsageRepository.findAll()).thenReturn(List.of(
                WorkOrderPartUsage.builder()
                        .id(1L)
                        .workOrder(workOrder)
                        .part(Part.builder().id(2L).partNumber("P-01").name("Filter").build())
                        .quantityUsed(1)
                        .totalCost(new BigDecimal("10.00"))
                        .build(),
                WorkOrderPartUsage.builder()
                        .id(2L)
                        .workOrder(workOrder)
                        .part(Part.builder().id(3L).partNumber("P-02").name("Belt").build())
                        .quantityUsed(1)
                        .totalCost(null)
                        .build()
        ));
        when(workOrderExpenseRepository.findAll()).thenReturn(List.of(
                WorkOrderExpense.builder()
                        .id(1L)
                        .workOrder(workOrder)
                        .costType(CostType.PART)
                        .amount(new BigDecimal("5.00"))
                        .build(),
                WorkOrderExpense.builder()
                        .id(2L)
                        .workOrder(workOrder)
                        .costType(CostType.LABOR)
                        .amount(new BigDecimal("20.00"))
                        .build(),
                WorkOrderExpense.builder()
                        .id(3L)
                        .workOrder(workOrder)
                        .costType(CostType.EXTERNAL_SERVICE)
                        .amount(new BigDecimal("30.00"))
                        .build(),
                WorkOrderExpense.builder()
                        .id(4L)
                        .workOrder(workOrder)
                        .costType(CostType.MISC)
                        .amount(new BigDecimal("2.50"))
                        .build()
        ));

        DashboardOverviewResponseDto response = reportService.getDashboardSummary();

        assertEquals(3L, response.getTotalVehicles());
        assertEquals(9L, response.getTotalWorkOrders());
        assertEquals(1L, response.getUpcomingMaintenanceCount());
        assertEquals(1L, response.getOverdueMaintenanceCount());
        assertEquals(1L, response.getLowStockItemCount());
        assertEquals(new BigDecimal("15.00"), response.getPartCostTotal());
        assertEquals(new BigDecimal("20.00"), response.getLaborCostTotal());
        assertEquals(new BigDecimal("30.00"), response.getExternalServiceCostTotal());
        assertEquals(new BigDecimal("2.50"), response.getMiscCostTotal());
        assertEquals(new BigDecimal("67.50"), response.getGrandTotalMaintenanceCost());
    }

    @Test
    void workOrderStatusSummaryReturnsCountsForAllStatuses() {
        when(workOrderRepository.countByStatus(WorkOrderStatus.OPEN)).thenReturn(1L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.ASSIGNED)).thenReturn(2L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.IN_PROGRESS)).thenReturn(3L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED)).thenReturn(4L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.CANCELLED)).thenReturn(5L);

        WorkOrderStatusSummaryResponseDto response = reportService.getWorkOrderStatusSummary();

        assertEquals(15L, response.getTotalWorkOrders());
        assertEquals(5, response.getItems().size());
        assertEquals(WorkOrderStatus.OPEN, response.getItems().getFirst().getStatus());
        assertEquals(1L, response.getItems().getFirst().getCount());
    }

    @Test
    void recentCompletedWorkOrdersReturnsTopCompletedInDescendingOrder() {
        Vehicle vehicle = buildVehicle(1L, "34XYZ789", "Mercedes", "Sprinter");
        WorkOrder newer = WorkOrder.builder()
                .id(10L)
                .vehicle(vehicle)
                .status(WorkOrderStatus.COMPLETED)
                .title("Brake job")
                .completionNotes("Done")
                .completedAt(LocalDateTime.of(2026, 6, 17, 12, 0))
                .build();
        WorkOrder older = WorkOrder.builder()
                .id(9L)
                .vehicle(vehicle)
                .status(WorkOrderStatus.COMPLETED)
                .title("Oil change")
                .completionNotes("Done")
                .completedAt(LocalDateTime.of(2026, 6, 16, 12, 0))
                .build();

        when(workOrderRepository.findByStatusOrderByCompletedAtDesc(any(WorkOrderStatus.class), any(Pageable.class)))
                .thenReturn(List.of(newer, older));

        List<RecentCompletedWorkOrderResponseDto> response = reportService.getRecentCompletedWorkOrders();

        assertEquals(2, response.size());
        assertEquals(10L, response.getFirst().getWorkOrderId());
        assertEquals("34XYZ789 - Mercedes Sprinter", response.getFirst().getVehicleDisplayName());
    }

    @Test
    void maintenanceStatusSummaryUsesMaintenanceCalculationService() {
        Vehicle vehicle = buildVehicle(1L, "06AAA06", "Isuzu", "D-Max");
        MaintenanceSchedule first = MaintenanceSchedule.builder().id(1L).vehicle(vehicle).active(true).build();
        MaintenanceSchedule second = MaintenanceSchedule.builder().id(2L).vehicle(vehicle).active(true).build();

        when(maintenanceScheduleRepository.findByActiveTrue()).thenReturn(List.of(first, second));
        when(maintenanceCalculationService.calculateStatus(vehicle, first))
                .thenReturn(CalculatedMaintenanceStatus.ON_TRACK);
        when(maintenanceCalculationService.calculateStatus(vehicle, second))
                .thenReturn(CalculatedMaintenanceStatus.OVERDUE);

        MaintenanceStatusSummaryResponseDto response = reportService.getMaintenanceStatusSummary();

        assertEquals(2L, response.getTotalActiveSchedules());
        assertEquals(3, response.getItems().size());
        verify(maintenanceCalculationService).calculateStatus(vehicle, first);
        verify(maintenanceCalculationService).calculateStatus(vehicle, second);
    }

    @Test
    void vehicleMaintenanceCostsCombineUsageAndExpensesPerVehicle() {
        Vehicle firstVehicle = buildVehicle(1L, "34AAA34", "Ford", "Transit");
        Vehicle secondVehicle = buildVehicle(2L, "35BBB35", "Fiat", "Doblo");

        when(workOrderPartUsageRepository.findAll()).thenReturn(List.of(
                WorkOrderPartUsage.builder()
                        .workOrder(WorkOrder.builder().vehicle(firstVehicle).build())
                        .part(Part.builder().id(1L).partNumber("P1").name("Pad").build())
                        .quantityUsed(2)
                        .totalCost(new BigDecimal("10.00"))
                        .build(),
                WorkOrderPartUsage.builder()
                        .workOrder(WorkOrder.builder().vehicle(secondVehicle).build())
                        .part(Part.builder().id(2L).partNumber("P2").name("Oil").build())
                        .quantityUsed(1)
                        .totalCost(null)
                        .build()
        ));
        when(workOrderExpenseRepository.findAll()).thenReturn(List.of(
                WorkOrderExpense.builder()
                        .workOrder(WorkOrder.builder().vehicle(firstVehicle).build())
                        .costType(CostType.LABOR)
                        .amount(new BigDecimal("20.00"))
                        .build(),
                WorkOrderExpense.builder()
                        .workOrder(WorkOrder.builder().vehicle(secondVehicle).build())
                        .costType(CostType.MISC)
                        .amount(new BigDecimal("3.00"))
                        .build()
        ));

        List<VehicleMaintenanceCostReportDto> response = reportService.getVehicleMaintenanceCosts();

        assertEquals(2, response.size());
        assertEquals("34AAA34 - Ford Transit", response.getFirst().getVehicleDisplayName());
        assertEquals(new BigDecimal("30.00"), response.getFirst().getGrandTotal());
        assertEquals(new BigDecimal("3.00"), response.get(1).getGrandTotal());
    }

    @Test
    void partConsumptionReportSumsQuantityAndNullSafeCost() {
        Part part = Part.builder().id(5L).partNumber("P-5").name("Filter").build();
        Vehicle vehicle = buildVehicle(1L, "34CCC34", "Renault", "Master");

        when(workOrderPartUsageRepository.findAll()).thenReturn(List.of(
                WorkOrderPartUsage.builder()
                        .workOrder(WorkOrder.builder().vehicle(vehicle).build())
                        .part(part)
                        .quantityUsed(2)
                        .totalCost(new BigDecimal("4.00"))
                        .build(),
                WorkOrderPartUsage.builder()
                        .workOrder(WorkOrder.builder().vehicle(vehicle).build())
                        .part(part)
                        .quantityUsed(3)
                        .totalCost(null)
                        .build()
        ));

        List<PartConsumptionReportDto> response = reportService.getPartConsumptionReport();

        assertEquals(1, response.size());
        assertEquals(5, response.getFirst().getTotalQuantityUsed());
        assertEquals(new BigDecimal("4.00"), response.getFirst().getTotalCost());
    }

    @Test
    void lowStockSummaryMapsItemsAndCount() {
        when(inventoryItemRepository.findLowStockItems()).thenReturn(List.of(
                InventoryItem.builder()
                        .id(9L)
                        .part(Part.builder().id(1L).partNumber("P-9").name("Bearing").build())
                        .currentQuantity(1)
                        .minimumStockLevel(4)
                        .location("Shelf A")
                        .build()
        ));

        LowStockSummaryResponseDto response = reportService.getLowStockSummary();

        assertEquals(1L, response.getLowStockItemCount());
        assertEquals("Shelf A", response.getItems().getFirst().getLocation());
    }

    @Test
    void emptyReportsReturnEmptySafeResponses() {
        when(vehicleRepository.count()).thenReturn(0L);
        when(workOrderRepository.countByStatus(any(WorkOrderStatus.class))).thenReturn(0L);
        when(maintenanceScheduleRepository.findByActiveTrue()).thenReturn(List.of());
        when(inventoryItemRepository.findLowStockItems()).thenReturn(List.of());
        when(workOrderPartUsageRepository.findAll()).thenReturn(List.of());
        when(workOrderExpenseRepository.findAll()).thenReturn(List.of());

        DashboardOverviewResponseDto dashboard = reportService.getDashboardSummary();
        LowStockSummaryResponseDto lowStock = reportService.getLowStockSummary();
        List<PartConsumptionReportDto> partConsumption = reportService.getPartConsumptionReport();
        List<VehicleMaintenanceCostReportDto> vehicleCosts = reportService.getVehicleMaintenanceCosts();

        assertEquals(BigDecimal.ZERO, dashboard.getGrandTotalMaintenanceCost());
        assertEquals(0L, lowStock.getLowStockItemCount());
        assertEquals(0, partConsumption.size());
        assertEquals(0, vehicleCosts.size());
        verify(maintenanceCalculationService, never()).calculateStatus(any(Vehicle.class), any(MaintenanceSchedule.class));
    }

    private Vehicle buildVehicle(Long id, String plateNumber, String brand, String model) {
        return Vehicle.builder()
                .id(id)
                .plateNumber(plateNumber)
                .brand(brand)
                .model(model)
                .build();
    }
}
