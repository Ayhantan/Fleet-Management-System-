package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.WorkOrderPartUsageRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderPartUsageResponseDto;
import com.ayhan.fleet_management.entity.InventoryItem;
import com.ayhan.fleet_management.entity.Part;
import com.ayhan.fleet_management.entity.StockMovement;
import com.ayhan.fleet_management.entity.WorkOrder;
import com.ayhan.fleet_management.entity.WorkOrderPartUsage;
import com.ayhan.fleet_management.entity.enums.StockMovementType;
import com.ayhan.fleet_management.entity.enums.WorkOrderStatus;
import com.ayhan.fleet_management.exception.InsufficientStockException;
import com.ayhan.fleet_management.exception.InvalidWorkOrderStateException;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.InventoryItemRepository;
import com.ayhan.fleet_management.repository.PartRepository;
import com.ayhan.fleet_management.repository.StockMovementRepository;
import com.ayhan.fleet_management.repository.WorkOrderPartUsageRepository;
import com.ayhan.fleet_management.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderPartUsageServiceImpl implements WorkOrderPartUsageService {

    private static final Set<WorkOrderStatus> CONSUMABLE_WORK_ORDER_STATUSES = EnumSet.of(
            WorkOrderStatus.OPEN,
            WorkOrderStatus.ASSIGNED,
            WorkOrderStatus.IN_PROGRESS
    );

    private final WorkOrderPartUsageRepository workOrderPartUsageRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PartRepository partRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    public WorkOrderPartUsageResponseDto consumePart(Long workOrderId, WorkOrderPartUsageRequestDto requestDto) {
        WorkOrder workOrder = findWorkOrderById(workOrderId);
        validateWorkOrderStatus(workOrder);

        Part part = findPartById(requestDto.getPartId());
        InventoryItem inventoryItem = findInventoryItemByPartId(part.getId());
        validateSufficientStock(inventoryItem, requestDto.getQuantityUsed());

        inventoryItem.setCurrentQuantity(inventoryItem.getCurrentQuantity() - requestDto.getQuantityUsed());

        StockMovement stockMovement = StockMovement.builder()
                .part(part)
                .inventoryItem(inventoryItem)
                .workOrder(workOrder)
                .type(StockMovementType.OUT)
                .quantity(requestDto.getQuantityUsed())
                .notes(requestDto.getNotes())
                .build();

        inventoryItemRepository.save(inventoryItem);
        StockMovement savedMovement = stockMovementRepository.save(stockMovement);

        WorkOrderPartUsage usage = WorkOrderPartUsage.builder()
                .workOrder(workOrder)
                .part(part)
                .inventoryItem(inventoryItem)
                .stockMovement(savedMovement)
                .quantityUsed(requestDto.getQuantityUsed())
                .notes(requestDto.getNotes())
                .build();

        return mapToResponseDto(workOrderPartUsageRepository.save(usage));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderPartUsageResponseDto> getPartsByWorkOrder(Long workOrderId) {
        findWorkOrderById(workOrderId);
        return workOrderPartUsageRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private void validateWorkOrderStatus(WorkOrder workOrder) {
        if (!CONSUMABLE_WORK_ORDER_STATUSES.contains(workOrder.getStatus())) {
            throw new InvalidWorkOrderStateException(
                    "Parts can only be consumed for work orders in OPEN, ASSIGNED, or IN_PROGRESS status"
            );
        }
    }

    private void validateSufficientStock(InventoryItem inventoryItem, Integer quantity) {
        if (inventoryItem.getCurrentQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for part id: " + inventoryItem.getPart().getId()
            );
        }
    }

    private WorkOrder findWorkOrderById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));
    }

    private Part findPartById(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));
    }

    private InventoryItem findInventoryItemByPartId(Long partId) {
        return inventoryItemRepository.findByPartId(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found for part id: " + partId));
    }

    private WorkOrderPartUsageResponseDto mapToResponseDto(WorkOrderPartUsage usage) {
        return WorkOrderPartUsageResponseDto.builder()
                .id(usage.getId())
                .workOrderId(usage.getWorkOrder().getId())
                .partId(usage.getPart().getId())
                .partNumber(usage.getPart().getPartNumber())
                .partName(usage.getPart().getName())
                .inventoryItemId(usage.getInventoryItem().getId())
                .stockMovementId(usage.getStockMovement().getId())
                .quantityUsed(usage.getQuantityUsed())
                .notes(usage.getNotes())
                .createdAt(usage.getCreatedAt())
                .build();
    }
}
