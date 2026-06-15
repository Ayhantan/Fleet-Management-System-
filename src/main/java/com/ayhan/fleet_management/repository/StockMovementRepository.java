package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    boolean existsByPartId(Long partId);

    boolean existsByInventoryItemId(Long inventoryItemId);

    List<StockMovement> findByPartIdOrderByCreatedAtDesc(Long partId);
}
