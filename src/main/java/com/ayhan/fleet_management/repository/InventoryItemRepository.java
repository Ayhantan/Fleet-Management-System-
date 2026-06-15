package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    boolean existsByPartId(Long partId);

    Optional<InventoryItem> findByPartId(Long partId);

    @Query("select i from InventoryItem i where i.currentQuantity <= i.minimumStockLevel order by i.currentQuantity asc, i.id asc")
    List<InventoryItem> findLowStockItems();
}
