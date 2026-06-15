package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.WorkOrderPartUsageRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderPartUsageResponseDto;

import java.util.List;

public interface WorkOrderPartUsageService {

    WorkOrderPartUsageResponseDto consumePart(Long workOrderId, WorkOrderPartUsageRequestDto requestDto);

    List<WorkOrderPartUsageResponseDto> getPartsByWorkOrder(Long workOrderId);
}
