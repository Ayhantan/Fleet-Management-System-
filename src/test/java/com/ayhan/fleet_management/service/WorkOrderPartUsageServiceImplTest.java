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
import com.ayhan.fleet_management.exception.InvalidWorkOrderStateException;
import com.ayhan.fleet_management.repository.InventoryItemRepository;
import com.ayhan.fleet_management.repository.PartRepository;
import com.ayhan.fleet_management.repository.StockMovementRepository;
import com.ayhan.fleet_management.repository.WorkOrderPartUsageRepository;
import com.ayhan.fleet_management.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderPartUsageServiceImplTest {

    @Mock
    private WorkOrderPartUsageRepository workOrderPartUsageRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private WorkOrderPartUsageServiceImpl workOrderPartUsageService;

    @Test
    void consumePartWhenWorkOrderIsCompletedThrowsInvalidWorkOrderStateException() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(7L)
                .status(WorkOrderStatus.COMPLETED)
                .build();

        WorkOrderPartUsageRequestDto requestDto = WorkOrderPartUsageRequestDto.builder()
                .partId(3L)
                .quantityUsed(1)
                .notes("Used during closeout")
                .build();

        when(workOrderRepository.findById(7L)).thenReturn(Optional.of(workOrder));

        assertThrows(
                InvalidWorkOrderStateException.class,
                () -> workOrderPartUsageService.consumePart(7L, requestDto)
        );

        verify(workOrderRepository).findById(7L);
        verify(partRepository, never()).findById(any());
    }

    @Test
    void consumePartWhenWorkOrderIsActiveCreatesUsageAndStockMovement() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(10L)
                .status(WorkOrderStatus.IN_PROGRESS)
                .build();

        Part part = Part.builder()
                .id(2L)
                .partNumber("FLT-002")
                .name("Hydraulic Filter")
                .build();

        InventoryItem inventoryItem = InventoryItem.builder()
                .id(20L)
                .part(part)
                .currentQuantity(5)
                .minimumStockLevel(1)
                .build();

        WorkOrderPartUsageRequestDto requestDto = WorkOrderPartUsageRequestDto.builder()
                .partId(2L)
                .quantityUsed(2)
                .unitCost(new BigDecimal("7.50"))
                .notes("Replaced during repair")
                .build();

        when(workOrderRepository.findById(10L)).thenReturn(Optional.of(workOrder));
        when(partRepository.findById(2L)).thenReturn(Optional.of(part));
        when(inventoryItemRepository.findByPartId(2L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement movement = invocation.getArgument(0);
            movement.setId(100L);
            return movement;
        });
        when(workOrderPartUsageRepository.save(any(WorkOrderPartUsage.class))).thenAnswer(invocation -> {
            WorkOrderPartUsage usage = invocation.getArgument(0);
            usage.setId(200L);
            return usage;
        });

        WorkOrderPartUsageResponseDto responseDto = workOrderPartUsageService.consumePart(10L, requestDto);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        StockMovement savedMovement = movementCaptor.getValue();
        assertEquals(StockMovementType.OUT, savedMovement.getType());
        assertEquals(2, savedMovement.getQuantity());
        assertEquals(workOrder, savedMovement.getWorkOrder());
        assertEquals(3, inventoryItem.getCurrentQuantity());
        assertEquals(200L, responseDto.getId());
        assertEquals(100L, responseDto.getStockMovementId());
        assertEquals(10L, responseDto.getWorkOrderId());
        assertEquals(2, responseDto.getQuantityUsed());
        assertEquals(new BigDecimal("7.50"), responseDto.getUnitCost());
        assertEquals(new BigDecimal("15.00"), responseDto.getTotalCost());
    }
}
