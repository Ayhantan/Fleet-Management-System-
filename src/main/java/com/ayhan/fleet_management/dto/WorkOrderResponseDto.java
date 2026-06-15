package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderResponseDto {

    private Long id;
    private Long vehicleId;
    private String vehicleName;
    private Long maintenanceTaskId;
    private String maintenanceTaskTitle;
    private Long assignedUserId;
    private String assignedUsername;
    private WorkOrderStatus status;
    private String title;
    private String description;
    private String completionNotes;
    private BigDecimal actualCost;
    private BigDecimal laborHours;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
