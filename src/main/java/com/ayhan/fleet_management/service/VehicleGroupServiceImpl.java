package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.VehicleGroupRequestDto;
import com.ayhan.fleet_management.dto.VehicleGroupResponseDto;
import com.ayhan.fleet_management.entity.VehicleGroup;
import com.ayhan.fleet_management.exception.DuplicateResourceException;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.VehicleGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleGroupServiceImpl implements VehicleGroupService {

    private final VehicleGroupRepository vehicleGroupRepository;

    @Override
    public VehicleGroupResponseDto createVehicleGroup(VehicleGroupRequestDto requestDto) {
        validateUniqueName(requestDto.getName(), null);

        VehicleGroup vehicleGroup = VehicleGroup.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .build();

        return mapToResponseDto(vehicleGroupRepository.save(vehicleGroup));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleGroupResponseDto> getAllVehicleGroups() {
        return vehicleGroupRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleGroupResponseDto getVehicleGroupById(Long id) {
        return mapToResponseDto(findVehicleGroupById(id));
    }

    @Override
    public VehicleGroupResponseDto updateVehicleGroup(Long id, VehicleGroupRequestDto requestDto) {
        VehicleGroup vehicleGroup = findVehicleGroupById(id);
        validateUniqueName(requestDto.getName(), id);

        vehicleGroup.setName(requestDto.getName());
        vehicleGroup.setDescription(requestDto.getDescription());

        return mapToResponseDto(vehicleGroupRepository.save(vehicleGroup));
    }

    @Override
    public void deleteVehicleGroup(Long id) {
        VehicleGroup vehicleGroup = findVehicleGroupById(id);
        vehicleGroupRepository.delete(vehicleGroup);
    }

    private VehicleGroup findVehicleGroupById(Long id) {
        return vehicleGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle group not found with id: " + id));
    }

    private void validateUniqueName(String name, Long currentGroupId) {
        boolean exists = vehicleGroupRepository.findAll()
                .stream()
                .anyMatch(group -> group.getName().equalsIgnoreCase(name)
                        && (currentGroupId == null || !group.getId().equals(currentGroupId)));

        if (exists) {
            throw new DuplicateResourceException("Vehicle group name is already in use");
        }
    }

    private VehicleGroupResponseDto mapToResponseDto(VehicleGroup vehicleGroup) {
        return VehicleGroupResponseDto.builder()
                .id(vehicleGroup.getId())
                .name(vehicleGroup.getName())
                .description(vehicleGroup.getDescription())
                .createdAt(vehicleGroup.getCreatedAt())
                .updatedAt(vehicleGroup.getUpdatedAt())
                .build();
    }
}
