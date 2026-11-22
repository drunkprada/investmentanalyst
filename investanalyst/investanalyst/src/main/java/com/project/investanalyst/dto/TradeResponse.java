package com.project.investanalyst.dto;

import com.project.investanalyst.entity.TradeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse(
        Long id,
        Long portfolioId,
        String ticker,
        TradeType type,
        BigDecimal quantity,
        BigDecimal executionPrice,
        LocalDateTime executionTime
) {}
