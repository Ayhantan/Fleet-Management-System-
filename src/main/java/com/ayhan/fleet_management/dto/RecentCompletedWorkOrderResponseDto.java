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
public class RecentCompletedWorkOrderResponseDto {

    private Long workOrderId;
    private Long vehicleId;
    private String vehicleDisplayName;
    private Long maintenanceTaskId;
    private String maintenanceTaskTitle;
    private String title;
    private String completionNotes;
    private LocalDateTime completedAt;
}
