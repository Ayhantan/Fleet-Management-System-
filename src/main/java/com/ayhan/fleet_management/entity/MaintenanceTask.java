package com.ayhan.fleet_management.entity;

import com.ayhan.fleet_management.entity.enums.MaintenancePriority;
import com.ayhan.fleet_management.entity.enums.MaintenanceTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_schedule_id")
    private MaintenanceSchedule maintenanceSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_definition_id")
    private MaintenanceDefinition maintenanceDefinition;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceTaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenancePriority priority;

    private LocalDate plannedDate;

    private LocalDate dueDate;

    private Integer dueHourMeter;

    private Integer dueDistanceReading;

    private LocalDate completedDate;

    private Integer completedHourMeter;

    private Integer completedDistanceReading;

    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = MaintenanceTaskStatus.PLANNED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
