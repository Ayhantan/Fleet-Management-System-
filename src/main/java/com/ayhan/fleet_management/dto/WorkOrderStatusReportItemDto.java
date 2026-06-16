package com.ayhan.fleet_management.dto;

import com.ayhan.fleet_management.entity.enums.WorkOrderStatus;
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
public class WorkOrderStatusReportItemDto {

    private WorkOrderStatus status;
    private long count;
}
