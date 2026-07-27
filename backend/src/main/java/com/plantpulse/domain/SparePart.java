package com.plantpulse.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stockQuantity;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer minStockThreshold;

    @DecimalMin("0.0")
    private Double unitCost;

    public boolean isBelowThreshold() {
        return stockQuantity != null && minStockThreshold != null
                && stockQuantity < minStockThreshold;
    }
}
