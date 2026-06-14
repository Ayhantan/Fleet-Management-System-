package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByPlateNumber(String plateNumber);
}
