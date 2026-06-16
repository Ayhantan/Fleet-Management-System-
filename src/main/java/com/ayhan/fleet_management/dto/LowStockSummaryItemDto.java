package com.ayhan.fleet_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockSummaryItemDto {

    private Long inventoryItemId;
    private Long partId;
    private String partNumber;
    private String partName;
    private Integer currentQuantity;
    private Integer minimumStockLevel;
    private String location;
}
