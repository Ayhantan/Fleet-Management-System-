package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.PartRequestDto;
import com.ayhan.fleet_management.dto.PartResponseDto;
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
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WorkOrderPartUsageRepository workOrderPartUsageRepository;

    @Override
    public PartResponseDto createPart(PartRequestDto requestDto) {
        validateUniquePartNumber(requestDto.getPartNumber(), null);

        Part part = Part.builder()
                .partNumber(requestDto.getPartNumber())
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .unit(requestDto.getUnit())
                .build();

        return mapToResponseDto(partRepository.save(part));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartResponseDto> getAllParts() {
        return partRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PartResponseDto getPartById(Long id) {
        return mapToResponseDto(findPartById(id));
    }

    @Override
    public PartResponseDto updatePart(Long id, PartRequestDto requestDto) {
        Part part = findPartById(id);
        validateUniquePartNumber(requestDto.getPartNumber(), id);

        part.setPartNumber(requestDto.getPartNumber());
        part.setName(requestDto.getName());
        part.setDescription(requestDto.getDescription());
        part.setUnit(requestDto.getUnit());

        return mapToResponseDto(partRepository.save(part));
    }

    @Override
    public void deletePart(Long id) {
        Part part = findPartById(id);

        if (inventoryItemRepository.existsByPartId(id)
                || stockMovementRepository.existsByPartId(id)
                || workOrderPartUsageRepository.existsByPartId(id)) {
            throw new ResourceInUseException("Part cannot be deleted because it is referenced by inventory or history");
        }

        partRepository.delete(part);
    }

    private Part findPartById(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));
    }

    private void validateUniquePartNumber(String partNumber, Long currentPartId) {
        boolean exists = partRepository.findAll()
                .stream()
                .anyMatch(part -> part.getPartNumber().equalsIgnoreCase(partNumber)
                        && (currentPartId == null || !part.getId().equals(currentPartId)));

        if (exists) {
            throw new DuplicateResourceException("Part number is already in use");
        }
    }

    private PartResponseDto mapToResponseDto(Part part) {
        return PartResponseDto.builder()
                .id(part.getId())
                .partNumber(part.getPartNumber())
                .name(part.getName())
                .description(part.getDescription())
                .unit(part.getUnit())
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }
}
