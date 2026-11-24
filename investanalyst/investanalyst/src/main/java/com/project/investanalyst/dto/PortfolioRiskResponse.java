package com.project.investanalyst.dto;
import java.time.LocalDate;
import java.util.Map;
public record PortfolioRiskResponse(
        Long portfolioId,
        String currency,
        int observations,
        LocalDate startDate,
        LocalDate endDate,
        double investedValue,
        double cash,
        double annualizedVolatility,
        double historicalVaR95,
        double largestHoldingConcentration,
        Map<String, Double> holdingWeights
) {}
