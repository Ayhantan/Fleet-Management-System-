package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.StockMovementRequestDto;
import com.ayhan.fleet_management.dto.StockMovementResponseDto;
import com.ayhan.fleet_management.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping("/in")
    public ResponseEntity<StockMovementResponseDto> stockIn(
            @Valid @RequestBody StockMovementRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockMovementService.stockIn(requestDto));
    }

    @PostMapping("/out")
    public ResponseEntity<StockMovementResponseDto> stockOut(
            @Valid @RequestBody StockMovementRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockMovementService.stockOut(requestDto));
    }

    @GetMapping("/part/{partId}")
    public ResponseEntity<List<StockMovementResponseDto>> getStockMovementsByPart(@PathVariable Long partId) {
        return ResponseEntity.ok(stockMovementService.getStockMovementsByPart(partId));
    }
}
