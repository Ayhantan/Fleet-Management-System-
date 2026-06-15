package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.entity.Part;
import com.ayhan.fleet_management.exception.ResourceInUseException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartServiceImplTest {

    @Mock
    private PartRepository partRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private WorkOrderPartUsageRepository workOrderPartUsageRepository;

    @InjectMocks
    private PartServiceImpl partService;

    @Test
    void deletePartWhenReferencedThrowsResourceInUseException() {
        Part part = Part.builder()
                .id(1L)
                .partNumber("FLT-001")
                .name("Oil Filter")
                .build();

        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(inventoryItemRepository.existsByPartId(1L)).thenReturn(true);

        assertThrows(ResourceInUseException.class, () -> partService.deletePart(1L));

        verify(partRepository).findById(1L);
        verify(inventoryItemRepository).existsByPartId(1L);
        verify(partRepository, never()).delete(part);
    }
}
