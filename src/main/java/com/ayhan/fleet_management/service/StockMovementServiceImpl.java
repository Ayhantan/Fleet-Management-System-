package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.StockMovementRequestDto;
import com.ayhan.fleet_management.dto.StockMovementResponseDto;
import com.ayhan.fleet_management.entity.InventoryItem;
import com.ayhan.fleet_management.entity.Part;
import com.ayhan.fleet_management.entity.StockMovement;
import com.ayhan.fleet_management.entity.enums.StockMovementType;
import com.ayhan.fleet_management.exception.InsufficientStockException;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.InventoryItemRepository;
import com.ayhan.fleet_management.repository.PartRepository;
import com.ayhan.fleet_management.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final PartRepository partRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Override
    public StockMovementResponseDto stockIn(StockMovementRequestDto requestDto) {
        Part part = findPartById(requestDto.getPartId());
        InventoryItem inventoryItem = findInventoryItemByPartId(part.getId());

        inventoryItem.setCurrentQuantity(inventoryItem.getCurrentQuantity() + requestDto.getQuantity());

        StockMovement stockMovement = StockMovement.builder()
                .part(part)
                .inventoryItem(inventoryItem)
                .type(StockMovementType.IN)
                .quantity(requestDto.getQuantity())
                .notes(requestDto.getNotes())
                .build();

        inventoryItemRepository.save(inventoryItem);
        return mapToResponseDto(stockMovementRepository.save(stockMovement));
    }

    @Override
    public StockMovementResponseDto stockOut(StockMovementRequestDto requestDto) {
        Part part = findPartById(requestDto.getPartId());
        InventoryItem inventoryItem = findInventoryItemByPartId(part.getId());
        validateSufficientStock(inventoryItem, requestDto.getQuantity());

        inventoryItem.setCurrentQuantity(inventoryItem.getCurrentQuantity() - requestDto.getQuantity());

        StockMovement stockMovement = StockMovement.builder()
                .part(part)
                .inventoryItem(inventoryItem)
                .type(StockMovementType.OUT)
                .quantity(requestDto.getQuantity())
                .notes(requestDto.getNotes())
                .build();

        inventoryItemRepository.save(inventoryItem);
        return mapToResponseDto(stockMovementRepository.save(stockMovement));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponseDto> getStockMovementsByPart(Long partId) {
        findPartById(partId);
        return stockMovementRepository.findByPartIdOrderByCreatedAtDesc(partId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private void validateSufficientStock(InventoryItem inventoryItem, Integer quantity) {
        if (inventoryItem.getCurrentQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for part id: " + inventoryItem.getPart().getId()
            );
        }
    }

    private Part findPartById(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));
    }

    private InventoryItem findInventoryItemByPartId(Long partId) {
        return inventoryItemRepository.findByPartId(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found for part id: " + partId));
    }

    private StockMovementResponseDto mapToResponseDto(StockMovement stockMovement) {
        return StockMovementResponseDto.builder()
                .id(stockMovement.getId())
                .partId(stockMovement.getPart().getId())
                .partNumber(stockMovement.getPart().getPartNumber())
                .partName(stockMovement.getPart().getName())
                .inventoryItemId(stockMovement.getInventoryItem().getId())
                .workOrderId(stockMovement.getWorkOrder() != null ? stockMovement.getWorkOrder().getId() : null)
                .type(stockMovement.getType())
                .quantity(stockMovement.getQuantity())
                .notes(stockMovement.getNotes())
                .createdAt(stockMovement.getCreatedAt())
                .build();
    }
}
