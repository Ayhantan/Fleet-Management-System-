package com.ayhan.fleet_management.entity;

import com.ayhan.fleet_management.entity.enums.DistanceUnit;
import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.entity.enums.TimeIntervalUnit;
import com.ayhan.fleet_management.entity.enums.VehicleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
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
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String plateNumber;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer modelYear;

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_group_id")
    private VehicleGroup vehicleGroup;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String category;

    private Integer currentHourMeter;

    private Integer currentDistanceReading;

    private LocalDate lastMaintenanceDate;

    private Integer lastMaintenanceHourMeter;

    private Integer lastMaintenanceDistanceReading;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceTriggerType maintenanceTriggerType;

    private Integer hourIntervalValue;

    private Integer distanceIntervalValue;

    private Integer timeIntervalValue;

    @Enumerated(EnumType.STRING)
    private TimeIntervalUnit timeIntervalUnit;

    @Enumerated(EnumType.STRING)
    private DistanceUnit distanceUnit;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
