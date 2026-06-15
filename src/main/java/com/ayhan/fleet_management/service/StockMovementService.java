package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.StockMovementRequestDto;
import com.ayhan.fleet_management.dto.StockMovementResponseDto;

import java.util.List;

public interface StockMovementService {

    StockMovementResponseDto stockIn(StockMovementRequestDto requestDto);

    StockMovementResponseDto stockOut(StockMovementRequestDto requestDto);

    List<StockMovementResponseDto> getStockMovementsByPart(Long partId);
}
