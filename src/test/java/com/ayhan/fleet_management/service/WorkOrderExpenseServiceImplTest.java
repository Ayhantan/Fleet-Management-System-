package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.WorkOrderCostSummaryResponseDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseCreateRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseResponseDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseUpdateRequestDto;
import com.ayhan.fleet_management.entity.WorkOrder;
import com.ayhan.fleet_management.entity.WorkOrderExpense;
import com.ayhan.fleet_management.entity.WorkOrderPartUsage;
import com.ayhan.fleet_management.entity.enums.CostType;
import com.ayhan.fleet_management.entity.enums.WorkOrderStatus;
import com.ayhan.fleet_management.exception.InvalidExpenseException;
import com.ayhan.fleet_management.exception.InvalidWorkOrderStateException;
import com.ayhan.fleet_management.repository.WorkOrderExpenseRepository;
import com.ayhan.fleet_management.repository.WorkOrderPartUsageRepository;
import com.ayhan.fleet_management.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderExpenseServiceImplTest {

    @Mock
    private WorkOrderExpenseRepository workOrderExpenseRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkOrderPartUsageRepository workOrderPartUsageRepository;

    @InjectMocks
    private WorkOrderExpenseServiceImpl workOrderExpenseService;

    @Test
    void addingPositiveExpenseSucceeds() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(1L)
                .status(WorkOrderStatus.OPEN)
                .build();

        WorkOrderExpenseCreateRequestDto requestDto = WorkOrderExpenseCreateRequestDto.builder()
                .costType(CostType.LABOR)
                .description("Technician labor")
                .amount(new BigDecimal("150.00"))
                .build();

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder));
        when(workOrderExpenseRepository.save(any(WorkOrderExpense.class))).thenAnswer(invocation -> {
            WorkOrderExpense expense = invocation.getArgument(0);
            expense.setId(11L);
            return expense;
        });

        WorkOrderExpenseResponseDto responseDto = workOrderExpenseService.createExpense(1L, requestDto);

        assertEquals(11L, responseDto.getId());
        assertEquals(1L, responseDto.getWorkOrderId());
        assertEquals(CostType.LABOR, responseDto.getCostType());
        assertEquals(new BigDecimal("150.00"), responseDto.getAmount());
    }

    @Test
    void zeroExpenseFails() {
        WorkOrderExpenseCreateRequestDto requestDto = WorkOrderExpenseCreateRequestDto.builder()
                .costType(CostType.MISC)
                .description("Invalid")
                .amount(BigDecimal.ZERO)
                .build();

        assertThrows(InvalidExpenseException.class, () -> workOrderExpenseService.createExpense(1L, requestDto));
    }

    @Test
    void negativeExpenseFails() {
        WorkOrderExpenseCreateRequestDto requestDto = WorkOrderExpenseCreateRequestDto.builder()
                .costType(CostType.MISC)
                .description("Invalid")
                .amount(new BigDecimal("-1.00"))
                .build();

        assertThrows(InvalidExpenseException.class, () -> workOrderExpenseService.createExpense(1L, requestDto));
    }

    @Test
    void addingExpenseToCompletedWorkOrderFails() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(2L)
                .status(WorkOrderStatus.COMPLETED)
                .build();

        WorkOrderExpenseCreateRequestDto requestDto = WorkOrderExpenseCreateRequestDto.builder()
                .costType(CostType.LABOR)
                .description("Technician labor")
                .amount(new BigDecimal("50.00"))
                .build();

        when(workOrderRepository.findById(2L)).thenReturn(Optional.of(workOrder));

        assertThrows(
                InvalidWorkOrderStateException.class,
                () -> workOrderExpenseService.createExpense(2L, requestDto)
        );
    }

    @Test
    void addingExpenseToCancelledWorkOrderFails() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(3L)
                .status(WorkOrderStatus.CANCELLED)
                .build();

        WorkOrderExpenseCreateRequestDto requestDto = WorkOrderExpenseCreateRequestDto.builder()
                .costType(CostType.EXTERNAL_SERVICE)
                .description("Towing")
                .amount(new BigDecimal("75.00"))
                .build();

        when(workOrderRepository.findById(3L)).thenReturn(Optional.of(workOrder));

        assertThrows(
                InvalidWorkOrderStateException.class,
                () -> workOrderExpenseService.createExpense(3L, requestDto)
        );
    }

    @Test
    void updatingExpenseOnActiveWorkOrderSucceeds() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(4L)
                .status(WorkOrderStatus.IN_PROGRESS)
                .build();

        WorkOrderExpense expense = WorkOrderExpense.builder()
                .id(21L)
                .workOrder(workOrder)
                .costType(CostType.MISC)
                .description("Old description")
                .amount(new BigDecimal("20.00"))
                .build();

        WorkOrderExpenseUpdateRequestDto requestDto = WorkOrderExpenseUpdateRequestDto.builder()
                .costType(CostType.LABOR)
                .description("Updated labor")
                .amount(new BigDecimal("120.00"))
                .build();

        when(workOrderExpenseRepository.findById(21L)).thenReturn(Optional.of(expense));
        when(workOrderExpenseRepository.save(any(WorkOrderExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderExpenseResponseDto responseDto = workOrderExpenseService.updateExpense(21L, requestDto);

        assertEquals(CostType.LABOR, responseDto.getCostType());
        assertEquals("Updated labor", responseDto.getDescription());
        assertEquals(new BigDecimal("120.00"), responseDto.getAmount());
    }

    @Test
    void updatingExpenseOnCompletedWorkOrderFails() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(5L)
                .status(WorkOrderStatus.COMPLETED)
                .build();

        WorkOrderExpense expense = WorkOrderExpense.builder()
                .id(31L)
                .workOrder(workOrder)
                .build();

        WorkOrderExpenseUpdateRequestDto requestDto = WorkOrderExpenseUpdateRequestDto.builder()
                .costType(CostType.MISC)
                .description("Blocked update")
                .amount(new BigDecimal("30.00"))
                .build();

        when(workOrderExpenseRepository.findById(31L)).thenReturn(Optional.of(expense));

        assertThrows(
                InvalidWorkOrderStateException.class,
                () -> workOrderExpenseService.updateExpense(31L, requestDto)
        );
    }

    @Test
    void deletingExpenseOnTerminalWorkOrderFails() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(6L)
                .status(WorkOrderStatus.CANCELLED)
                .build();

        WorkOrderExpense expense = WorkOrderExpense.builder()
                .id(41L)
                .workOrder(workOrder)
                .build();

        when(workOrderExpenseRepository.findById(41L)).thenReturn(Optional.of(expense));

        assertThrows(
                InvalidWorkOrderStateException.class,
                () -> workOrderExpenseService.deleteExpense(41L)
        );

        verify(workOrderExpenseRepository, never()).delete(expense);
    }

    @Test
    void costSummaryCalculatesLaborExternalServiceMiscPartAndGrandTotalsCorrectly() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(7L)
                .status(WorkOrderStatus.OPEN)
                .build();

        WorkOrderPartUsage usageOne = WorkOrderPartUsage.builder()
                .id(1L)
                .workOrder(workOrder)
                .totalCost(new BigDecimal("10.50"))
                .build();

        WorkOrderPartUsage usageTwo = WorkOrderPartUsage.builder()
                .id(2L)
                .workOrder(workOrder)
                .totalCost(new BigDecimal("9.50"))
                .build();

        WorkOrderExpense labor = WorkOrderExpense.builder()
                .id(1L)
                .workOrder(workOrder)
                .costType(CostType.LABOR)
                .amount(new BigDecimal("25.00"))
                .build();

        WorkOrderExpense external = WorkOrderExpense.builder()
                .id(2L)
                .workOrder(workOrder)
                .costType(CostType.EXTERNAL_SERVICE)
                .amount(new BigDecimal("40.00"))
                .build();

        WorkOrderExpense misc = WorkOrderExpense.builder()
                .id(3L)
                .workOrder(workOrder)
                .costType(CostType.MISC)
                .amount(new BigDecimal("5.00"))
                .build();

        WorkOrderExpense partExpense = WorkOrderExpense.builder()
                .id(4L)
                .workOrder(workOrder)
                .costType(CostType.PART)
                .amount(new BigDecimal("3.00"))
                .build();

        when(workOrderRepository.findById(7L)).thenReturn(Optional.of(workOrder));
        when(workOrderPartUsageRepository.findByWorkOrderIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(usageOne, usageTwo));
        when(workOrderExpenseRepository.findByWorkOrderIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(labor, external, misc, partExpense));

        WorkOrderCostSummaryResponseDto responseDto = workOrderExpenseService.getCostSummary(7L);

        assertEquals(new BigDecimal("23.00"), responseDto.getPartCostTotal());
        assertEquals(new BigDecimal("25.00"), responseDto.getLaborCostTotal());
        assertEquals(new BigDecimal("40.00"), responseDto.getExternalServiceCostTotal());
        assertEquals(new BigDecimal("5.00"), responseDto.getMiscCostTotal());
        assertEquals(new BigDecimal("93.00"), responseDto.getGrandTotal());
    }

    @Test
    void nullPartUsageTotalCostIsTreatedAsZeroInSummary() {
        WorkOrder workOrder = WorkOrder.builder()
                .id(8L)
                .status(WorkOrderStatus.ASSIGNED)
                .build();

        WorkOrderPartUsage usage = WorkOrderPartUsage.builder()
                .id(5L)
                .workOrder(workOrder)
                .totalCost(null)
                .build();

        WorkOrderExpense labor = WorkOrderExpense.builder()
                .id(5L)
                .workOrder(workOrder)
                .costType(CostType.LABOR)
                .amount(new BigDecimal("12.00"))
                .build();

        when(workOrderRepository.findById(8L)).thenReturn(Optional.of(workOrder));
        when(workOrderPartUsageRepository.findByWorkOrderIdOrderByCreatedAtDesc(8L))
                .thenReturn(List.of(usage));
        when(workOrderExpenseRepository.findByWorkOrderIdOrderByCreatedAtDesc(8L))
                .thenReturn(List.of(labor));

        WorkOrderCostSummaryResponseDto responseDto = workOrderExpenseService.getCostSummary(8L);

        assertEquals(BigDecimal.ZERO, responseDto.getPartCostTotal());
        assertEquals(new BigDecimal("12.00"), responseDto.getLaborCostTotal());
        assertEquals(new BigDecimal("12.00"), responseDto.getGrandTotal());
    }
}
