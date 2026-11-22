package com.project.investanalyst.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Entity
@Getter

@NoArgsConstructor
public class Holding {
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String ticker;
    @Column(nullable = false)
    private BigDecimal averageCost;
    @Column(nullable = false)
    private BigDecimal quantity;

    public  Holding(String ticker, BigDecimal averageCost, BigDecimal quantity, Portfolio portfolio) {
        if(ticker == null || ticker.isEmpty()){
            throw new IllegalArgumentException("ticker cannot be null");
        }
        if(averageCost == null||averageCost.signum() < 0){
            throw new IllegalArgumentException("averageCost cannot be null");
        }
        if(quantity == null|| quantity.signum() <= 0){
            throw new IllegalArgumentException("quantity cannot be null");
        }
        if(portfolio == null){
            throw new IllegalArgumentException("portfolio cannot be null");
        }
        this.ticker = ticker.trim().toUpperCase(Locale.ROOT);
        this.averageCost = averageCost;
        this.quantity = quantity;
        this.portfolio = portfolio;

    }

    public void existingBuy(BigDecimal newAverageCost, BigDecimal newQuantity){
        this.averageCost = averageCost.multiply(quantity)
                .add(newAverageCost.multiply(newQuantity))
                .divide(newQuantity.add(quantity), 2, RoundingMode.HALF_UP);
        this.quantity = quantity.add(newQuantity);
    }

}
