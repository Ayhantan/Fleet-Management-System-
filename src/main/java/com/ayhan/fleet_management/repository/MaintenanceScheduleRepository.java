package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    List<MaintenanceSchedule> findByVehicleId(Long vehicleId);

    List<MaintenanceSchedule> findByVehicleIdAndActiveTrue(Long vehicleId);

    List<MaintenanceSchedule> findByActiveTrue();
}
