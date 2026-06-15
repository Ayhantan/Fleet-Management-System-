package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementResponseDto {

    private Long id;
    private Long partId;
    private String partNumber;
    private String partName;
    private Long inventoryItemId;
    private Long workOrderId;
    private StockMovementType type;
    private Integer quantity;
    private String notes;
    private LocalDateTime createdAt;
}
