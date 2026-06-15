package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.PartRequestDto;
import com.ayhan.fleet_management.dto.PartResponseDto;
import com.ayhan.fleet_management.service.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    @PostMapping
    public ResponseEntity<PartResponseDto> createPart(@Valid @RequestBody PartRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partService.createPart(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<PartResponseDto>> getAllParts() {
        return ResponseEntity.ok(partService.getAllParts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartResponseDto> getPartById(@PathVariable Long id) {
        return ResponseEntity.ok(partService.getPartById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartResponseDto> updatePart(
            @PathVariable Long id,
            @Valid @RequestBody PartRequestDto requestDto
    ) {
        return ResponseEntity.ok(partService.updatePart(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable Long id) {
        partService.deletePart(id);
        return ResponseEntity.noContent().build();
    }
}
