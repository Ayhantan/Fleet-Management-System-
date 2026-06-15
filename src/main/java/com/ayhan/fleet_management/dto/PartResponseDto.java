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
public class PartResponseDto {

    private Long id;
    private String partNumber;
    private String name;
    private String description;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
