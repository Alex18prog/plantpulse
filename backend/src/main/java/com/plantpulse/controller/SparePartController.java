package com.plantpulse.controller;

import com.plantpulse.domain.SparePart;
import com.plantpulse.exception.ResourceNotFoundException;
import com.plantpulse.repository.SparePartRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spare-parts")
@RequiredArgsConstructor
@Tag(name = "Spare parts")
public class SparePartController {

    private final SparePartRepository sparePartRepository;

    @GetMapping
    public List<SparePart> findAll() {
        return sparePartRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a spare part (ADMIN only)")
    @ApiResponse(responseCode = "403", description = "Caller is not ADMIN")
    public SparePart create(@Valid @RequestBody SparePart sparePart) {
        sparePart.setId(null);
        return sparePartRepository.save(sparePart);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adjust stock quantity by a relative amount (ADMIN only)")
    @ApiResponse(responseCode = "403", description = "Caller is not ADMIN")
    public SparePart adjustStock(
            @PathVariable Long id,
            @Parameter(description = "Signed delta applied to the current stock, e.g. -1 to decrease, 1 to increase")
            @RequestParam int delta) {
        SparePart part = sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spare part not found: " + id));
        part.setStockQuantity(part.getStockQuantity() + delta);
        return sparePartRepository.save(part);
    }
}
