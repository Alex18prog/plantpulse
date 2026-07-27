package com.plantpulse.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spare_parts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer minStockThreshold;

    private Double unitCost;

    public boolean isBelowThreshold() {
        return stockQuantity != null && minStockThreshold != null
                && stockQuantity < minStockThreshold;
    }
}
