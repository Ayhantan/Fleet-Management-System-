package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.CreateWorkOrderFromMaintenanceTaskRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderAssignmentRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderCompletionRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderResponseDto;
import com.ayhan.fleet_management.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    public ResponseEntity<WorkOrderResponseDto> createWorkOrder(
            @Valid @RequestBody WorkOrderRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workOrderService.createWorkOrder(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<WorkOrderResponseDto>> getAllWorkOrders() {
        return ResponseEntity.ok(workOrderService.getAllWorkOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponseDto> getWorkOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.getWorkOrderById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkOrderResponseDto> updateWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderRequestDto requestDto
    ) {
        return ResponseEntity.ok(workOrderService.updateWorkOrder(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WorkOrderResponseDto> cancelWorkOrder(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.cancelWorkOrder(id));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<WorkOrderResponseDto> assignWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderAssignmentRequestDto requestDto
    ) {
        return ResponseEntity.ok(workOrderService.assignWorkOrder(id, requestDto));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<WorkOrderResponseDto> startWorkOrder(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.startWorkOrder(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<WorkOrderResponseDto> completeWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderCompletionRequestDto requestDto
    ) {
        return ResponseEntity.ok(workOrderService.completeWorkOrder(id, requestDto));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<WorkOrderResponseDto>> getWorkOrdersByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(workOrderService.getWorkOrdersByVehicle(vehicleId));
    }

    @PostMapping("/from-maintenance-task")
    public ResponseEntity<WorkOrderResponseDto> createWorkOrderFromMaintenanceTask(
            @Valid @RequestBody CreateWorkOrderFromMaintenanceTaskRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workOrderService.createWorkOrderFromMaintenanceTask(requestDto));
    }
}
