package com.project.investanalyst.dto;

import java.math.BigDecimal;

public record PortfolioResponse(
        Long id,
        String name,
        BigDecimal initialCash,
        BigDecimal availableCash,
        String currency
) {}
