package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.WorkOrderExpenseResponseDto;
import com.ayhan.fleet_management.dto.WorkOrderExpenseUpdateRequestDto;
import com.ayhan.fleet_management.service.WorkOrderExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-order-expenses")
@RequiredArgsConstructor
public class WorkOrderExpenseController {

    private final WorkOrderExpenseService workOrderExpenseService;

    @PutMapping("/{expenseId}")
    public ResponseEntity<WorkOrderExpenseResponseDto> updateExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody WorkOrderExpenseUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(workOrderExpenseService.updateExpense(expenseId, requestDto));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        workOrderExpenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
