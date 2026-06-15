package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.WorkOrderPartUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderPartUsageRepository extends JpaRepository<WorkOrderPartUsage, Long> {

    boolean existsByPartId(Long partId);

    boolean existsByInventoryItemId(Long inventoryItemId);

    List<WorkOrderPartUsage> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId);
}
