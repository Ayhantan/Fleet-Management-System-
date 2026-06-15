package com.ayhan.fleet_management.repository;

import com.ayhan.fleet_management.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {

    boolean existsByPartNumber(String partNumber);
}
