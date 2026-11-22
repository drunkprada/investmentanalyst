package com.project.investanalyst.dto;

import java.math.BigDecimal;

public record HoldingResponse(
        Long id,
        Long portfolioId,
        String ticker,
        BigDecimal averageCost,
        BigDecimal quantity
) {}
