package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.WorkOrder;
import com.ayhan.fleet_management.entity.enums.WorkOrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    List<WorkOrder> findAllByOrderByCreatedAtDesc();

    List<WorkOrder> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    long countByStatus(WorkOrderStatus status);

    List<WorkOrder> findByStatusOrderByCompletedAtDesc(WorkOrderStatus status, Pageable pageable);

    boolean existsByMaintenanceTaskIdAndStatusIn(Long maintenanceTaskId, Collection<WorkOrderStatus> statuses);
}
