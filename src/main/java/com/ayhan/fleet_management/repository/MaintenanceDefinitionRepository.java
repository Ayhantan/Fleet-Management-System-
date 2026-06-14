package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.MaintenanceDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceDefinitionRepository extends JpaRepository<MaintenanceDefinition, Long> {
}
