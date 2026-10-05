package com.project.portfolio.domain;
import java.time.Instant;
import java.math.BigDecimal;
public class Trade{
    String tradeId;
    String orderId;
    String portfolioId;
    String ticker;
    Side side;
    int quantity;
    BigDecimal executionPrice;
    Instant executedAt;
    public Trade(
        String tradeId,
        String orderId,
        String portfolioId,
        String ticker,
        Side side,
        int quantity,
        BigDecimal executionPrice,
        Instant executedAt
) {
    if (tradeId == null || tradeId.isBlank()) {
        throw new IllegalArgumentException("Trade ID cannot be blank");
    }
    if (orderId == null || orderId.isBlank()) {
        throw new IllegalArgumentException("Order ID cannot be blank");
    }
    if (portfolioId == null || portfolioId.isBlank()) {
        throw new IllegalArgumentException("Portfolio ID cannot be blank");
    }
    if (ticker == null || ticker.isBlank()) {
        throw new IllegalArgumentException("Ticker cannot be blank");
    }
    if (side == null) {
        throw new IllegalArgumentException("Side cannot be null");
    }
    if (quantity <= 0) {
        throw new IllegalArgumentException("Share quantity must be positive");
    }
    if (executionPrice == null ||
            executionPrice.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Execution price must be positive");
    }
    if (executedAt == null) {
        throw new IllegalArgumentException("Execution time cannot be null");
    }

    this.tradeId = tradeId;
    this.orderId = orderId;
    this.portfolioId = portfolioId;
    this.ticker = ticker;
    this.side = side;
    this.quantity = quantity;
    this.executionPrice = executionPrice;
    this.executedAt = executedAt;
}
}