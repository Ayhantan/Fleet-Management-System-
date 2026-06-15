package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.CreateWorkOrderFromMaintenanceTaskRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderAssignmentRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderCostSummaryResponseDto;
import com.ayhan.fleet_management.dto.WorkOrderCompletionRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseCreateRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseResponseDto;
import com.ayhan.fleet_management.dto.WorkOrderPartUsageRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderPartUsageResponseDto;
import com.ayhan.fleet_management.dto.WorkOrderRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderResponseDto;
import com.ayhan.fleet_management.service.WorkOrderExpenseService;
import com.ayhan.fleet_management.service.WorkOrderPartUsageService;
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
    private final WorkOrderPartUsageService workOrderPartUsageService;
    private final WorkOrderExpenseService workOrderExpenseService;

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

    @PostMapping("/{workOrderId}/parts")
    public ResponseEntity<WorkOrderPartUsageResponseDto> consumePart(
            @PathVariable Long workOrderId,
            @Valid @RequestBody WorkOrderPartUsageRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workOrderPartUsageService.consumePart(workOrderId, requestDto));
    }

    @GetMapping("/{workOrderId}/parts")
    public ResponseEntity<List<WorkOrderPartUsageResponseDto>> getPartsByWorkOrder(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(workOrderPartUsageService.getPartsByWorkOrder(workOrderId));
    }

    @PostMapping("/{workOrderId}/expenses")
    public ResponseEntity<WorkOrderExpenseResponseDto> createExpense(
            @PathVariable Long workOrderId,
            @Valid @RequestBody WorkOrderExpenseCreateRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workOrderExpenseService.createExpense(workOrderId, requestDto));
    }

    @GetMapping("/{workOrderId}/expenses")
    public ResponseEntity<List<WorkOrderExpenseResponseDto>> getExpensesByWorkOrder(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(workOrderExpenseService.getExpensesByWorkOrder(workOrderId));
    }

    @GetMapping("/{workOrderId}/cost-summary")
    public ResponseEntity<WorkOrderCostSummaryResponseDto> getCostSummary(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(workOrderExpenseService.getCostSummary(workOrderId));
    }
}
