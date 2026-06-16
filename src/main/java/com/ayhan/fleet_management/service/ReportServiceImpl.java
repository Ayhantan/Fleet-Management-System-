package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.DashboardOverviewResponseDto;
import com.ayhan.fleet_management.dto.LowStockSummaryItemDto;
import com.ayhan.fleet_management.dto.LowStockSummaryResponseDto;
import com.ayhan.fleet_management.dto.MaintenanceStatusReportItemDto;
import com.ayhan.fleet_management.dto.MaintenanceStatusSummaryResponseDto;
import com.ayhan.fleet_management.dto.PartConsumptionReportDto;
import com.ayhan.fleet_management.dto.RecentCompletedWorkOrderResponseDto;
import com.ayhan.fleet_management.dto.VehicleMaintenanceCostReportDto;
import com.ayhan.fleet_management.dto.WorkOrderStatusReportItemDto;
import com.ayhan.fleet_management.dto.WorkOrderStatusSummaryResponseDto;
import com.ayhan.fleet_management.entity.InventoryItem;
import com.ayhan.fleet_management.entity.MaintenanceSchedule;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int RECENT_COMPLETED_LIMIT = 10;

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderExpenseRepository workOrderExpenseRepository;
    private final WorkOrderPartUsageRepository workOrderPartUsageRepository;
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceCalculationService maintenanceCalculationService;

    @Override
    public DashboardOverviewResponseDto getDashboardSummary() {
        long openWorkOrders = workOrderRepository.countByStatus(WorkOrderStatus.OPEN);
        long assignedWorkOrders = workOrderRepository.countByStatus(WorkOrderStatus.ASSIGNED);
        long inProgressWorkOrders = workOrderRepository.countByStatus(WorkOrderStatus.IN_PROGRESS);
        long completedWorkOrders = workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED);
        long cancelledWorkOrders = workOrderRepository.countByStatus(WorkOrderStatus.CANCELLED);

        MaintenanceStatusSummaryResponseDto maintenanceSummary = getMaintenanceStatusSummary();
        LowStockSummaryResponseDto lowStockSummary = getLowStockSummary();
        CostTotals costTotals = calculateGlobalCostTotals();

        return DashboardOverviewResponseDto.builder()
                .totalVehicles(vehicleRepository.count())
                .totalWorkOrders(openWorkOrders + assignedWorkOrders + inProgressWorkOrders + completedWorkOrders + cancelledWorkOrders)
                .openWorkOrders(openWorkOrders)
                .assignedWorkOrders(assignedWorkOrders)
                .inProgressWorkOrders(inProgressWorkOrders)
                .completedWorkOrders(completedWorkOrders)
                .cancelledWorkOrders(cancelledWorkOrders)
                .upcomingMaintenanceCount(countMaintenanceStatus(maintenanceSummary, CalculatedMaintenanceStatus.UPCOMING))
                .overdueMaintenanceCount(countMaintenanceStatus(maintenanceSummary, CalculatedMaintenanceStatus.OVERDUE))
                .lowStockItemCount(lowStockSummary.getLowStockItemCount())
                .partCostTotal(costTotals.partCostTotal())
                .laborCostTotal(costTotals.laborCostTotal())
                .externalServiceCostTotal(costTotals.externalServiceCostTotal())
                .miscCostTotal(costTotals.miscCostTotal())
                .grandTotalMaintenanceCost(costTotals.grandTotal())
                .build();
    }

    @Override
    public WorkOrderStatusSummaryResponseDto getWorkOrderStatusSummary() {
        List<WorkOrderStatusReportItemDto> items = List.of(
                buildWorkOrderStatusItem(WorkOrderStatus.OPEN),
                buildWorkOrderStatusItem(WorkOrderStatus.ASSIGNED),
                buildWorkOrderStatusItem(WorkOrderStatus.IN_PROGRESS),
                buildWorkOrderStatusItem(WorkOrderStatus.COMPLETED),
                buildWorkOrderStatusItem(WorkOrderStatus.CANCELLED)
        );

        return WorkOrderStatusSummaryResponseDto.builder()
                .totalWorkOrders(items.stream().mapToLong(WorkOrderStatusReportItemDto::getCount).sum())
                .items(items)
                .build();
    }

    @Override
    public List<RecentCompletedWorkOrderResponseDto> getRecentCompletedWorkOrders() {
        return workOrderRepository.findByStatusOrderByCompletedAtDesc(
                        WorkOrderStatus.COMPLETED,
                        PageRequest.of(0, RECENT_COMPLETED_LIMIT)
                )
                .stream()
                .map(this::mapRecentCompletedWorkOrder)
                .toList();
    }

    @Override
    public MaintenanceStatusSummaryResponseDto getMaintenanceStatusSummary() {
        Map<CalculatedMaintenanceStatus, Long> counts = new EnumMap<>(CalculatedMaintenanceStatus.class);
        for (CalculatedMaintenanceStatus status : CalculatedMaintenanceStatus.values()) {
            counts.put(status, 0L);
        }

        List<MaintenanceSchedule> activeSchedules = maintenanceScheduleRepository.findByActiveTrue();
        for (MaintenanceSchedule schedule : activeSchedules) {
            CalculatedMaintenanceStatus status = maintenanceCalculationService.calculateStatus(
                    schedule.getVehicle(),
                    schedule
            );
            counts.put(status, counts.get(status) + 1L);
        }

        List<MaintenanceStatusReportItemDto> items = List.of(
                buildMaintenanceStatusItem(CalculatedMaintenanceStatus.ON_TRACK, counts),
                buildMaintenanceStatusItem(CalculatedMaintenanceStatus.UPCOMING, counts),
                buildMaintenanceStatusItem(CalculatedMaintenanceStatus.OVERDUE, counts)
        );

        return MaintenanceStatusSummaryResponseDto.builder()
                .totalActiveSchedules(activeSchedules.size())
                .items(items)
                .build();
    }

    @Override
    public List<VehicleMaintenanceCostReportDto> getVehicleMaintenanceCosts() {
        Map<Long, VehicleCostAccumulator> vehicleCosts = new HashMap<>();

        for (WorkOrderPartUsage usage : workOrderPartUsageRepository.findAll()) {
            Vehicle vehicle = usage.getWorkOrder().getVehicle();
            VehicleCostAccumulator accumulator = vehicleCosts.computeIfAbsent(
                    vehicle.getId(),
                    ignored -> new VehicleCostAccumulator(vehicle)
            );
            accumulator.partCostTotal = accumulator.partCostTotal.add(nullSafe(usage.getTotalCost()));
        }

        for (WorkOrderExpense expense : workOrderExpenseRepository.findAll()) {
            Vehicle vehicle = expense.getWorkOrder().getVehicle();
            VehicleCostAccumulator accumulator = vehicleCosts.computeIfAbsent(
                    vehicle.getId(),
                    ignored -> new VehicleCostAccumulator(vehicle)
            );
            BigDecimal amount = nullSafe(expense.getAmount());
            if (expense.getCostType() == CostType.PART) {
                accumulator.partCostTotal = accumulator.partCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.LABOR) {
                accumulator.laborCostTotal = accumulator.laborCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.EXTERNAL_SERVICE) {
                accumulator.externalServiceCostTotal = accumulator.externalServiceCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.MISC) {
                accumulator.miscCostTotal = accumulator.miscCostTotal.add(amount);
            }
        }

        return vehicleCosts.values()
                .stream()
                .sorted((left, right) -> left.vehicle.getId().compareTo(right.vehicle.getId()))
                .map(this::mapVehicleMaintenanceCost)
                .toList();
    }

    @Override
    public List<PartConsumptionReportDto> getPartConsumptionReport() {
        Map<Long, PartConsumptionAccumulator> partConsumption = new HashMap<>();

        for (WorkOrderPartUsage usage : workOrderPartUsageRepository.findAll()) {
            PartConsumptionAccumulator accumulator = partConsumption.computeIfAbsent(
                    usage.getPart().getId(),
                    ignored -> new PartConsumptionAccumulator(
                            usage.getPart().getId(),
                            usage.getPart().getPartNumber(),
                            usage.getPart().getName()
                    )
            );
            accumulator.totalQuantityUsed += usage.getQuantityUsed();
            accumulator.totalCost = accumulator.totalCost.add(nullSafe(usage.getTotalCost()));
        }

        return partConsumption.values()
                .stream()
                .sorted((left, right) -> left.partId.compareTo(right.partId))
                .map(accumulator -> PartConsumptionReportDto.builder()
                        .partId(accumulator.partId)
                        .partNumber(accumulator.partNumber)
                        .partName(accumulator.partName)
                        .totalQuantityUsed(accumulator.totalQuantityUsed)
                        .totalCost(accumulator.totalCost)
                        .build())
                .toList();
    }

    @Override
    public LowStockSummaryResponseDto getLowStockSummary() {
        List<LowStockSummaryItemDto> items = inventoryItemRepository.findLowStockItems()
                .stream()
                .map(this::mapLowStockItem)
                .toList();

        return LowStockSummaryResponseDto.builder()
                .lowStockItemCount(items.size())
                .items(items)
                .build();
    }

    private CostTotals calculateGlobalCostTotals() {
        BigDecimal partCostTotal = BigDecimal.ZERO;
        BigDecimal laborCostTotal = BigDecimal.ZERO;
        BigDecimal externalServiceCostTotal = BigDecimal.ZERO;
        BigDecimal miscCostTotal = BigDecimal.ZERO;

        for (WorkOrderPartUsage usage : workOrderPartUsageRepository.findAll()) {
            partCostTotal = partCostTotal.add(nullSafe(usage.getTotalCost()));
        }

        for (WorkOrderExpense expense : workOrderExpenseRepository.findAll()) {
            BigDecimal amount = nullSafe(expense.getAmount());
            if (expense.getCostType() == CostType.PART) {
                partCostTotal = partCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.LABOR) {
                laborCostTotal = laborCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.EXTERNAL_SERVICE) {
                externalServiceCostTotal = externalServiceCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.MISC) {
                miscCostTotal = miscCostTotal.add(amount);
            }
        }

        return new CostTotals(
                partCostTotal,
                laborCostTotal,
                externalServiceCostTotal,
                miscCostTotal,
                partCostTotal.add(laborCostTotal).add(externalServiceCostTotal).add(miscCostTotal)
        );
    }

    private long countMaintenanceStatus(
            MaintenanceStatusSummaryResponseDto summary,
            CalculatedMaintenanceStatus targetStatus
    ) {
        return summary.getItems()
                .stream()
                .filter(item -> item.getStatus() == targetStatus)
                .mapToLong(MaintenanceStatusReportItemDto::getCount)
                .sum();
    }

    private WorkOrderStatusReportItemDto buildWorkOrderStatusItem(WorkOrderStatus status) {
        return WorkOrderStatusReportItemDto.builder()
                .status(status)
                .count(workOrderRepository.countByStatus(status))
                .build();
    }

    private MaintenanceStatusReportItemDto buildMaintenanceStatusItem(
            CalculatedMaintenanceStatus status,
            Map<CalculatedMaintenanceStatus, Long> counts
    ) {
        return MaintenanceStatusReportItemDto.builder()
                .status(status)
                .count(counts.get(status))
                .build();
    }

    private RecentCompletedWorkOrderResponseDto mapRecentCompletedWorkOrder(WorkOrder workOrder) {
        return RecentCompletedWorkOrderResponseDto.builder()
                .workOrderId(workOrder.getId())
                .vehicleId(workOrder.getVehicle().getId())
                .vehicleDisplayName(buildVehicleDisplayName(workOrder.getVehicle()))
                .maintenanceTaskId(workOrder.getMaintenanceTask() != null ? workOrder.getMaintenanceTask().getId() : null)
                .maintenanceTaskTitle(workOrder.getMaintenanceTask() != null ? workOrder.getMaintenanceTask().getTitle() : null)
                .title(workOrder.getTitle())
                .completionNotes(workOrder.getCompletionNotes())
                .completedAt(workOrder.getCompletedAt())
                .build();
    }

    private VehicleMaintenanceCostReportDto mapVehicleMaintenanceCost(VehicleCostAccumulator accumulator) {
        BigDecimal grandTotal = accumulator.partCostTotal
                .add(accumulator.laborCostTotal)
                .add(accumulator.externalServiceCostTotal)
                .add(accumulator.miscCostTotal);

        return VehicleMaintenanceCostReportDto.builder()
                .vehicleId(accumulator.vehicle.getId())
                .vehicleDisplayName(buildVehicleDisplayName(accumulator.vehicle))
                .partCostTotal(accumulator.partCostTotal)
                .laborCostTotal(accumulator.laborCostTotal)
                .externalServiceCostTotal(accumulator.externalServiceCostTotal)
                .miscCostTotal(accumulator.miscCostTotal)
                .grandTotal(grandTotal)
                .build();
    }

    private LowStockSummaryItemDto mapLowStockItem(InventoryItem inventoryItem) {
        return LowStockSummaryItemDto.builder()
                .inventoryItemId(inventoryItem.getId())
                .partId(inventoryItem.getPart().getId())
                .partNumber(inventoryItem.getPart().getPartNumber())
                .partName(inventoryItem.getPart().getName())
                .currentQuantity(inventoryItem.getCurrentQuantity())
                .minimumStockLevel(inventoryItem.getMinimumStockLevel())
                .location(inventoryItem.getLocation())
                .build();
    }

    private String buildVehicleDisplayName(Vehicle vehicle) {
        return vehicle.getPlateNumber() + " - " + vehicle.getBrand() + " " + vehicle.getModel();
    }

    private BigDecimal nullSafe(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    // TODO: Replace in-memory service aggregation with JPQL grouping queries if report volume grows.
    private record CostTotals(
            BigDecimal partCostTotal,
            BigDecimal laborCostTotal,
            BigDecimal externalServiceCostTotal,
            BigDecimal miscCostTotal,
            BigDecimal grandTotal
    ) {
    }

    private static class VehicleCostAccumulator {

        private final Vehicle vehicle;
        private BigDecimal partCostTotal = BigDecimal.ZERO;
        private BigDecimal laborCostTotal = BigDecimal.ZERO;
        private BigDecimal externalServiceCostTotal = BigDecimal.ZERO;
        private BigDecimal miscCostTotal = BigDecimal.ZERO;

        private VehicleCostAccumulator(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
    }

    private static class PartConsumptionAccumulator {

        private final Long partId;
        private final String partNumber;
        private final String partName;
        private int totalQuantityUsed;
        private BigDecimal totalCost = BigDecimal.ZERO;

        private PartConsumptionAccumulator(Long partId, String partNumber, String partName) {
            this.partId = partId;
            this.partNumber = partNumber;
            this.partName = partName;
        }
    }
}
