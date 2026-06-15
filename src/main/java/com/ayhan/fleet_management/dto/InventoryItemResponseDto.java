package com.ayhan.fleet_management.dto;

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
public class InventoryItemResponseDto {

    private Long id;
    private Long partId;
    private String partNumber;
    private String partName;
    private Integer currentQuantity;
    private Integer minimumStockLevel;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
