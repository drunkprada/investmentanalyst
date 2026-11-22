
package com.project.investanalyst.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"portfolio_id", "ticker"}
        )
)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @NotBlank
    @Column(nullable = false)
    private String ticker;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal averageCost;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal quantity;

    public Holding(
            String ticker,
            BigDecimal averageCost,
            BigDecimal quantity,
            Portfolio portfolio) {

        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker cannot be blank");
        }

        if (averageCost == null || averageCost.signum() <= 0) {
            throw new IllegalArgumentException("Average cost must be positive");
        }

        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio cannot be null");
        }

        this.ticker = ticker.trim().toUpperCase(Locale.ROOT);
        this.averageCost = averageCost;
        this.quantity = quantity;
        this.portfolio = portfolio;
    }

    public void existingBuy(
            BigDecimal purchasePrice,
            BigDecimal newQuantity) {

        if (purchasePrice == null || purchasePrice.signum() <= 0 ||
                newQuantity == null || newQuantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Purchase price and quantity must be positive"
            );
        }

        BigDecimal totalQuantity = quantity.add(newQuantity);

        BigDecimal totalCost = averageCost.multiply(quantity)
                .add(purchasePrice.multiply(newQuantity));

        this.averageCost = totalCost.divide(
                totalQuantity, 8, RoundingMode.HALF_UP
        );

        this.quantity = totalQuantity;
    }

    public void reduceQuantity(BigDecimal soldQuantity) {

        if (soldQuantity == null || soldQuantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Sold quantity must be positive"
            );
        }

        if (soldQuantity.compareTo(quantity) > 0) {
            throw new IllegalArgumentException(
                    "Cannot sell more shares than owned"
            );
        }

        this.quantity = quantity.subtract(soldQuantity);
    }

}
