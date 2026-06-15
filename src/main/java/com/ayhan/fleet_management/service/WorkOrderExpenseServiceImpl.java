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
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.WorkOrderExpenseRepository;
import com.ayhan.fleet_management.repository.WorkOrderPartUsageRepository;
import com.ayhan.fleet_management.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderExpenseServiceImpl implements WorkOrderExpenseService {

    private final WorkOrderExpenseRepository workOrderExpenseRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderPartUsageRepository workOrderPartUsageRepository;

    @Override
    public WorkOrderExpenseResponseDto createExpense(Long workOrderId, WorkOrderExpenseCreateRequestDto requestDto) {
        validatePositiveAmount(requestDto.getAmount());

        WorkOrder workOrder = findWorkOrderById(workOrderId);
        ensureWorkOrderIsActive(workOrder, "add");

        WorkOrderExpense expense = WorkOrderExpense.builder()
                .workOrder(workOrder)
                .costType(requestDto.getCostType())
                .description(requestDto.getDescription())
                .amount(requestDto.getAmount())
                .build();

        return mapToResponseDto(workOrderExpenseRepository.save(expense));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderExpenseResponseDto> getExpensesByWorkOrder(Long workOrderId) {
        findWorkOrderById(workOrderId);
        return workOrderExpenseRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderCostSummaryResponseDto getCostSummary(Long workOrderId) {
        findWorkOrderById(workOrderId);

        BigDecimal partCostTotal = sumPartUsageCosts(workOrderId);
        BigDecimal laborCostTotal = BigDecimal.ZERO;
        BigDecimal externalServiceCostTotal = BigDecimal.ZERO;
        BigDecimal miscCostTotal = BigDecimal.ZERO;

        for (WorkOrderExpense expense : workOrderExpenseRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId)) {
            BigDecimal amount = nullSafe(expense.getAmount());
            if (expense.getCostType() == CostType.PART) {
                partCostTotal = partCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.LABOR) {
                laborCostTotal = laborCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.EXTERNAL_SERVICE) {
                externalServiceCostTotal = externalServiceCostTotal.add(amount);
            } else if (expense.getCostType() == CostType.MISC) {
                miscCostTotal = miscCostTotal.add(amount);
            }
        }

        BigDecimal grandTotal = partCostTotal
                .add(laborCostTotal)
                .add(externalServiceCostTotal)
                .add(miscCostTotal);

        return WorkOrderCostSummaryResponseDto.builder()
                .workOrderId(workOrderId)
                .partCostTotal(partCostTotal)
                .laborCostTotal(laborCostTotal)
                .externalServiceCostTotal(externalServiceCostTotal)
                .miscCostTotal(miscCostTotal)
                .grandTotal(grandTotal)
                .build();
    }

    @Override
    public WorkOrderExpenseResponseDto updateExpense(Long expenseId, WorkOrderExpenseUpdateRequestDto requestDto) {
        validatePositiveAmount(requestDto.getAmount());

        WorkOrderExpense expense = findExpenseById(expenseId);
        ensureWorkOrderIsActive(expense.getWorkOrder(), "update");

        expense.setCostType(requestDto.getCostType());
        expense.setDescription(requestDto.getDescription());
        expense.setAmount(requestDto.getAmount());

        return mapToResponseDto(workOrderExpenseRepository.save(expense));
    }

    @Override
    public void deleteExpense(Long expenseId) {
        WorkOrderExpense expense = findExpenseById(expenseId);
        ensureWorkOrderIsActive(expense.getWorkOrder(), "delete");
        workOrderExpenseRepository.delete(expense);
    }

    private BigDecimal sumPartUsageCosts(Long workOrderId) {
        BigDecimal total = BigDecimal.ZERO;
        for (WorkOrderPartUsage usage : workOrderPartUsageRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId)) {
            total = total.add(nullSafe(usage.getTotalCost()));
        }
        return total;
    }

    private BigDecimal nullSafe(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseException("Expense amount must be greater than 0");
        }
    }

    private void ensureWorkOrderIsActive(WorkOrder workOrder, String action) {
        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED || workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new InvalidWorkOrderStateException(
                    "Cannot " + action + " expense for work order with status: " + workOrder.getStatus()
            );
        }
    }

    private WorkOrder findWorkOrderById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));
    }

    private WorkOrderExpense findExpenseById(Long id) {
        return workOrderExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order expense not found with id: " + id));
    }

    private WorkOrderExpenseResponseDto mapToResponseDto(WorkOrderExpense expense) {
        return WorkOrderExpenseResponseDto.builder()
                .id(expense.getId())
                .workOrderId(expense.getWorkOrder().getId())
                .costType(expense.getCostType())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
