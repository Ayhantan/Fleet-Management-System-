package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.CalculatedMaintenanceStatus;
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
public class MaintenanceStatusReportItemDto {

    private CalculatedMaintenanceStatus status;
    private long count;
}
