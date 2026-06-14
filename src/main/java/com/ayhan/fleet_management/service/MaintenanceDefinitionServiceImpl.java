package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.MaintenanceDefinitionRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceDefinitionResponseDto;
import com.ayhan.fleet_management.entity.MaintenanceDefinition;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.MaintenanceDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceDefinitionServiceImpl implements MaintenanceDefinitionService {

    private final MaintenanceDefinitionRepository maintenanceDefinitionRepository;

    @Override
    public MaintenanceDefinitionResponseDto createMaintenanceDefinition(MaintenanceDefinitionRequestDto requestDto) {
        MaintenanceDefinition maintenanceDefinition = MaintenanceDefinition.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .applicableAssetType(requestDto.getApplicableAssetType())
                .active(requestDto.getActive() != null ? requestDto.getActive() : Boolean.TRUE)
                .build();

        return mapToResponseDto(maintenanceDefinitionRepository.save(maintenanceDefinition));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceDefinitionResponseDto> getAllMaintenanceDefinitions() {
        return maintenanceDefinitionRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceDefinitionResponseDto getMaintenanceDefinitionById(Long id) {
        return mapToResponseDto(findMaintenanceDefinitionById(id));
    }

    @Override
    public MaintenanceDefinitionResponseDto updateMaintenanceDefinition(Long id, MaintenanceDefinitionRequestDto requestDto) {
        MaintenanceDefinition maintenanceDefinition = findMaintenanceDefinitionById(id);
        maintenanceDefinition.setName(requestDto.getName());
        maintenanceDefinition.setDescription(requestDto.getDescription());
        maintenanceDefinition.setCategory(requestDto.getCategory());
        maintenanceDefinition.setApplicableAssetType(requestDto.getApplicableAssetType());
        maintenanceDefinition.setActive(requestDto.getActive() != null ? requestDto.getActive() : maintenanceDefinition.getActive());

        return mapToResponseDto(maintenanceDefinitionRepository.save(maintenanceDefinition));
    }

    @Override
    public void deleteMaintenanceDefinition(Long id) {
        maintenanceDefinitionRepository.delete(findMaintenanceDefinitionById(id));
    }

    private MaintenanceDefinition findMaintenanceDefinitionById(Long id) {
        return maintenanceDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance definition not found with id: " + id));
    }

    private MaintenanceDefinitionResponseDto mapToResponseDto(MaintenanceDefinition maintenanceDefinition) {
        return MaintenanceDefinitionResponseDto.builder()
                .id(maintenanceDefinition.getId())
                .name(maintenanceDefinition.getName())
                .description(maintenanceDefinition.getDescription())
                .category(maintenanceDefinition.getCategory())
                .applicableAssetType(maintenanceDefinition.getApplicableAssetType())
                .active(maintenanceDefinition.getActive())
                .createdAt(maintenanceDefinition.getCreatedAt())
                .updatedAt(maintenanceDefinition.getUpdatedAt())
                .build();
    }
}
