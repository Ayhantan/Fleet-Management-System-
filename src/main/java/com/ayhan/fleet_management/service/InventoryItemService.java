package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.InventoryItemRequestDto;
import com.ayhan.fleet_management.dto.InventoryItemResponseDto;

import java.util.List;

public interface InventoryItemService {

    InventoryItemResponseDto createInventoryItem(InventoryItemRequestDto requestDto);

    List<InventoryItemResponseDto> getAllInventoryItems();

    InventoryItemResponseDto getInventoryItemById(Long id);

    InventoryItemResponseDto updateInventoryItem(Long id, InventoryItemRequestDto requestDto);

    void deleteInventoryItem(Long id);

    List<InventoryItemResponseDto> getLowStockInventoryItems();
}
