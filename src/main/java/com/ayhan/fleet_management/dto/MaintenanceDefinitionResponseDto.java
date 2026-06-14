package com.ayhan.fleet_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceDefinitionResponseDto {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String applicableAssetType;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
