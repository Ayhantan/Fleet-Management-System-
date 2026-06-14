package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.VehicleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleGroupRepository extends JpaRepository<VehicleGroup, Long> {

    boolean existsByName(String name);
}
