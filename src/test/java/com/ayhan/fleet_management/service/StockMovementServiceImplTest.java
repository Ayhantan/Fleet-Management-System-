package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.StockMovementRequestDto;
import com.ayhan.fleet_management.entity.InventoryItem;
import com.ayhan.fleet_management.entity.Part;
import com.ayhan.fleet_management.exception.InsufficientStockException;
import com.ayhan.fleet_management.repository.InventoryItemRepository;
import com.ayhan.fleet_management.repository.PartRepository;
import com.ayhan.fleet_management.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceImplTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private StockMovementServiceImpl stockMovementService;

    @Test
    void stockOutWhenQuantityExceedsAvailableStockThrowsInsufficientStockException() {
        Part part = Part.builder()
                .id(2L)
                .partNumber("BRK-100")
                .name("Brake Pad")
                .build();

        InventoryItem inventoryItem = InventoryItem.builder()
                .id(4L)
                .part(part)
                .currentQuantity(3)
                .minimumStockLevel(1)
                .build();

        StockMovementRequestDto requestDto = StockMovementRequestDto.builder()
                .partId(2L)
                .quantity(5)
                .notes("Manual stock adjustment")
                .build();

        when(partRepository.findById(2L)).thenReturn(Optional.of(part));
        when(inventoryItemRepository.findByPartId(2L)).thenReturn(Optional.of(inventoryItem));

        assertThrows(InsufficientStockException.class, () -> stockMovementService.stockOut(requestDto));

        verify(partRepository).findById(2L);
        verify(inventoryItemRepository).findByPartId(2L);
        verify(inventoryItemRepository, never()).save(inventoryItem);
        verify(stockMovementRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
