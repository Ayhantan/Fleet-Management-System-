package com.ayhan.fleet_management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class InventoryItemRequestDto {

    @NotNull(message = "Part id is required")
    private Long partId;

    @NotNull(message = "Current quantity is required")
    @Min(value = 0, message = "Current quantity must be greater than or equal to 0")
    private Integer currentQuantity;

    @NotNull(message = "Minimum stock level is required")
    @Min(value = 0, message = "Minimum stock level must be greater than or equal to 0")
    private Integer minimumStockLevel;

    private String location;
}
