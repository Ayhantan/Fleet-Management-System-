package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.InventoryItemRequestDto;
import com.ayhan.fleet_management.dto.InventoryItemResponseDto;
import com.ayhan.fleet_management.service.InventoryItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-items")
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @PostMapping
    public ResponseEntity<InventoryItemResponseDto> createInventoryItem(
            @Valid @RequestBody InventoryItemRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryItemService.createInventoryItem(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponseDto>> getAllInventoryItems() {
        return ResponseEntity.ok(inventoryItemService.getAllInventoryItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemResponseDto> getInventoryItemById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryItemService.getInventoryItemById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemResponseDto> updateInventoryItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemRequestDto requestDto
    ) {
        return ResponseEntity.ok(inventoryItemService.updateInventoryItem(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        inventoryItemService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemResponseDto>> getLowStockInventoryItems() {
        return ResponseEntity.ok(inventoryItemService.getLowStockInventoryItems());
    }
}
