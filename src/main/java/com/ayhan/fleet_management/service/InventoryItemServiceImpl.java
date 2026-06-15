package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.InventoryItemRequestDto;
import com.ayhan.fleet_management.dto.InventoryItemResponseDto;
import com.ayhan.fleet_management.entity.InventoryItem;
import com.ayhan.fleet_management.entity.Part;
import com.ayhan.fleet_management.exception.DuplicateResourceException;
import com.ayhan.fleet_management.exception.ResourceInUseException;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.InventoryItemRepository;
import com.ayhan.fleet_management.repository.PartRepository;
import com.ayhan.fleet_management.repository.StockMovementRepository;
import com.ayhan.fleet_management.repository.WorkOrderPartUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final PartRepository partRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WorkOrderPartUsageRepository workOrderPartUsageRepository;

    @Override
    public InventoryItemResponseDto createInventoryItem(InventoryItemRequestDto requestDto) {
        validateUniquePartUsage(requestDto.getPartId(), null);

        InventoryItem inventoryItem = InventoryItem.builder()
                .part(findPartById(requestDto.getPartId()))
                .currentQuantity(requestDto.getCurrentQuantity())
                .minimumStockLevel(requestDto.getMinimumStockLevel())
                .location(requestDto.getLocation())
                .build();

        return mapToResponseDto(inventoryItemRepository.save(inventoryItem));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponseDto> getAllInventoryItems() {
        return inventoryItemRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponseDto getInventoryItemById(Long id) {
        return mapToResponseDto(findInventoryItemById(id));
    }

    @Override
    public InventoryItemResponseDto updateInventoryItem(Long id, InventoryItemRequestDto requestDto) {
        InventoryItem inventoryItem = findInventoryItemById(id);
        validateUniquePartUsage(requestDto.getPartId(), id);

        inventoryItem.setPart(findPartById(requestDto.getPartId()));
        inventoryItem.setCurrentQuantity(requestDto.getCurrentQuantity());
        inventoryItem.setMinimumStockLevel(requestDto.getMinimumStockLevel());
        inventoryItem.setLocation(requestDto.getLocation());

        return mapToResponseDto(inventoryItemRepository.save(inventoryItem));
    }

    @Override
    public void deleteInventoryItem(Long id) {
        InventoryItem inventoryItem = findInventoryItemById(id);

        if (stockMovementRepository.existsByInventoryItemId(id)
                || workOrderPartUsageRepository.existsByInventoryItemId(id)) {
            throw new ResourceInUseException(
                    "Inventory item cannot be deleted because it is referenced by stock history or work order usage"
            );
        }

        inventoryItemRepository.delete(inventoryItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponseDto> getLowStockInventoryItems() {
        return inventoryItemRepository.findLowStockItems()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private InventoryItem findInventoryItemById(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));
    }

    private Part findPartById(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));
    }

    private void validateUniquePartUsage(Long partId, Long currentInventoryItemId) {
        inventoryItemRepository.findByPartId(partId)
                .ifPresent(existing -> {
                    if (currentInventoryItemId == null || !existing.getId().equals(currentInventoryItemId)) {
                        throw new DuplicateResourceException("An inventory item already exists for this part");
                    }
                });
    }

    private InventoryItemResponseDto mapToResponseDto(InventoryItem inventoryItem) {
        return InventoryItemResponseDto.builder()
                .id(inventoryItem.getId())
                .partId(inventoryItem.getPart().getId())
                .partNumber(inventoryItem.getPart().getPartNumber())
                .partName(inventoryItem.getPart().getName())
                .currentQuantity(inventoryItem.getCurrentQuantity())
                .minimumStockLevel(inventoryItem.getMinimumStockLevel())
                .location(inventoryItem.getLocation())
                .createdAt(inventoryItem.getCreatedAt())
                .updatedAt(inventoryItem.getUpdatedAt())
                .build();
    }
}
