package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.InventoryItemRequestDto;
import com.ayhan.fleet_management.entity.InventoryItem;
import com.ayhan.fleet_management.exception.DuplicateResourceException;
import com.ayhan.fleet_management.repository.InventoryItemRepository;
import com.ayhan.fleet_management.repository.PartRepository;
import com.ayhan.fleet_management.repository.StockMovementRepository;
import com.ayhan.fleet_management.repository.WorkOrderPartUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceImplTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private WorkOrderPartUsageRepository workOrderPartUsageRepository;

    @InjectMocks
    private InventoryItemServiceImpl inventoryItemService;

    @Test
    void createInventoryItemWhenPartAlreadyHasInventoryItemThrowsDuplicateResourceException() {
        InventoryItemRequestDto requestDto = InventoryItemRequestDto.builder()
                .partId(1L)
                .currentQuantity(10)
                .minimumStockLevel(2)
                .location("Main Warehouse")
                .build();

        when(inventoryItemRepository.findByPartId(1L))
                .thenReturn(Optional.of(InventoryItem.builder().id(5L).build()));

        assertThrows(DuplicateResourceException.class, () -> inventoryItemService.createInventoryItem(requestDto));

        verify(inventoryItemRepository).findByPartId(1L);
        verifyNoInteractions(partRepository, stockMovementRepository, workOrderPartUsageRepository);
    }
}
