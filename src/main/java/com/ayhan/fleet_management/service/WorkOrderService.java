package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.CreateWorkOrderFromMaintenanceTaskRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderAssignmentRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderCompletionRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderResponseDto;

import java.util.List;

public interface WorkOrderService {

    WorkOrderResponseDto createWorkOrder(WorkOrderRequestDto requestDto);

    List<WorkOrderResponseDto> getAllWorkOrders();

    WorkOrderResponseDto getWorkOrderById(Long id);

    WorkOrderResponseDto updateWorkOrder(Long id, WorkOrderRequestDto requestDto);

    WorkOrderResponseDto cancelWorkOrder(Long id);

    WorkOrderResponseDto assignWorkOrder(Long id, WorkOrderAssignmentRequestDto requestDto);

    WorkOrderResponseDto startWorkOrder(Long id);

    WorkOrderResponseDto completeWorkOrder(Long id, WorkOrderCompletionRequestDto requestDto);

    List<WorkOrderResponseDto> getWorkOrdersByVehicle(Long vehicleId);

    WorkOrderResponseDto createWorkOrderFromMaintenanceTask(CreateWorkOrderFromMaintenanceTaskRequestDto requestDto);
}
