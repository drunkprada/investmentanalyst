
package com.project.investanalyst.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal initialCash;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableCash;

    @Column(nullable = false, length = 3)
    private String currency;

    public void deductCash(BigDecimal purchase){
        this.availableCash = availableCash.subtract(purchase);
    }
    public Portfolio(String name, BigDecimal initialCash) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Portfolio name cannot be empty"
            );
        }

        if (initialCash == null ||
                initialCash.signum() < 0 ||
                initialCash.scale() > 2) {
            throw new IllegalArgumentException(
                    "Initial cash must be non-negative with at most 2 decimal places"
            );
        }

        this.name = name;
        this.initialCash = initialCash;
        this.availableCash = initialCash;
        this.currency = "USD";
    }
}
