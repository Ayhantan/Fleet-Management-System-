package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.MaintenanceTask;
import com.ayhan.fleet_management.entity.enums.MaintenanceTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {

    List<MaintenanceTask> findByVehicleId(Long vehicleId);

    List<MaintenanceTask> findByVehicleIdAndStatusOrderByCompletedDateDescCreatedAtDesc(
            Long vehicleId,
            MaintenanceTaskStatus status
    );

    Optional<MaintenanceTask> findTopByMaintenanceScheduleIdAndStatusOrderByCompletedDateDescCreatedAtDesc(
            Long maintenanceScheduleId,
            MaintenanceTaskStatus status
    );
}
