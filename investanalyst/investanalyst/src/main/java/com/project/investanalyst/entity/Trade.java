
package com.project.investanalyst.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType type;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal quantity;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal executionPrice;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime executionTime;

    public Trade(
            Portfolio portfolio,
            String ticker,
            TradeType type,
            BigDecimal quantity,
            BigDecimal executionPrice
    ) {
        if (portfolio == null || type == null ||
                ticker == null || ticker.isBlank() ||
                quantity == null || quantity.signum() <= 0 ||
                executionPrice == null || executionPrice.signum() <= 0) {
            throw new IllegalArgumentException("Invalid trade details");
        }

        this.portfolio = portfolio;
        this.ticker = ticker.trim().toUpperCase(Locale.ROOT);
        this.type = type;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.executionTime = LocalDateTime.now();
    }
}
