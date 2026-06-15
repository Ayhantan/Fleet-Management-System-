package com.ayhan.fleet_management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartRequestDto {

    @NotBlank(message = "Part number is required")
    private String partNumber;

    @NotBlank(message = "Part name is required")
    private String name;

    private String description;

    private String unit;
}
