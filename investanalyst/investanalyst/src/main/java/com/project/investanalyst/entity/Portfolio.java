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

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal initialCash;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableCash;

    @Column(nullable = false, length = 3)
    private String currency;

    public Portfolio(String name, BigDecimal initialCash) {
        if (name == null || name.isBlank() || name.length() > 120) {
            throw new IllegalArgumentException("Portfolio name must contain 1–120 characters");
        }
        if (initialCash == null || initialCash.signum() < 0 || initialCash.scale() > 2
                || initialCash.precision() - initialCash.scale() > 17) {
            throw new IllegalArgumentException("Initial cash must be non-negative with at most 2 decimal places");
        }
        this.name = name.trim();
        this.initialCash = initialCash;
        this.availableCash = initialCash;
        this.currency = "USD";
    }

    public void deductCash(BigDecimal purchase) {
        if (purchase == null || purchase.signum() <= 0) {
            throw new IllegalArgumentException("Purchase must be positive");
        }
        if (availableCash.compareTo(purchase) < 0) {
            throw new IllegalArgumentException("Insufficient cash");
        }
        this.availableCash = availableCash.subtract(purchase);
    }

    public void addCash(BigDecimal sale) {
        if (sale == null || sale.signum() <= 0) {
            throw new IllegalArgumentException("Sale must be positive");
        }
        this.availableCash = availableCash.add(sale);
    }
}
