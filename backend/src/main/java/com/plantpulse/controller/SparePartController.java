package com.plantpulse.controller;

import com.plantpulse.domain.SparePart;
import com.plantpulse.exception.ResourceNotFoundException;
import com.plantpulse.repository.SparePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spare-parts")
@RequiredArgsConstructor
public class SparePartController {

    private final SparePartRepository sparePartRepository;

    @GetMapping
    public List<SparePart> findAll() {
        return sparePartRepository.findAll();
    }

    @PostMapping
    public SparePart create(@RequestBody SparePart sparePart) {
        sparePart.setId(null);
        return sparePartRepository.save(sparePart);
    }

    @PatchMapping("/{id}/stock")
    public SparePart adjustStock(@PathVariable Long id, @RequestParam int delta) {
        SparePart part = sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spare part not found: " + id));
        part.setStockQuantity(part.getStockQuantity() + delta);
        return sparePartRepository.save(part);
    }
}
