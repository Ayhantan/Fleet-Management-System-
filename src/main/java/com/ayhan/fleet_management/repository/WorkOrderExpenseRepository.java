package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.WorkOrderExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderExpenseRepository extends JpaRepository<WorkOrderExpense, Long> {

    List<WorkOrderExpense> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId);
}
