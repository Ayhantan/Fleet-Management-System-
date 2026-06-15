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
public class WorkOrderPartUsageResponseDto {

    private Long id;
    private Long workOrderId;
    private Long partId;
    private String partNumber;
    private String partName;
    private Long inventoryItemId;
    private Long stockMovementId;
    private Integer quantityUsed;
    private String notes;
    private LocalDateTime createdAt;
}
