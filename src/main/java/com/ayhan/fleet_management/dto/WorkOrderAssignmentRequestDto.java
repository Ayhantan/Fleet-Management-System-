package com.ayhan.fleet_management.dto;

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
public class WorkOrderAssignmentRequestDto {

    @NotNull(message = "Assigned user id is required")
    private Long assignedUserId;
}
