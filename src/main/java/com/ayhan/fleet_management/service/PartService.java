package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.PartRequestDto;
import com.ayhan.fleet_management.dto.PartResponseDto;

import java.util.List;

public interface PartService {

    PartResponseDto createPart(PartRequestDto requestDto);

    List<PartResponseDto> getAllParts();

    PartResponseDto getPartById(Long id);

    PartResponseDto updatePart(Long id, PartRequestDto requestDto);

    void deletePart(Long id);
}
