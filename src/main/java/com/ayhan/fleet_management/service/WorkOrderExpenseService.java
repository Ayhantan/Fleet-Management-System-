package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.WorkOrderCostSummaryResponseDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseCreateRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseResponseDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseUpdateRequestDto;

import java.util.List;

public interface WorkOrderExpenseService {

    WorkOrderExpenseResponseDto createExpense(Long workOrderId, WorkOrderExpenseCreateRequestDto requestDto);

    List<WorkOrderExpenseResponseDto> getExpensesByWorkOrder(Long workOrderId);

    WorkOrderCostSummaryResponseDto getCostSummary(Long workOrderId);

    WorkOrderExpenseResponseDto updateExpense(Long expenseId, WorkOrderExpenseUpdateRequestDto requestDto);

    void deleteExpense(Long expenseId);
}
