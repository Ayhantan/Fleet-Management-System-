package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.MaintenanceDefinitionRequestDto;
import com.ayhan.fleet_management.dto.MaintenanceDefinitionResponseDto;

import java.util.List;

public interface MaintenanceDefinitionService {

    MaintenanceDefinitionResponseDto createMaintenanceDefinition(MaintenanceDefinitionRequestDto requestDto);

    List<MaintenanceDefinitionResponseDto> getAllMaintenanceDefinitions();

    MaintenanceDefinitionResponseDto getMaintenanceDefinitionById(Long id);

    MaintenanceDefinitionResponseDto updateMaintenanceDefinition(Long id, MaintenanceDefinitionRequestDto requestDto);

    void deleteMaintenanceDefinition(Long id);
}
