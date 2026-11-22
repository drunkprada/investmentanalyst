package com.project.investanalyst.dto;

import com.project.investanalyst.entity.Portfolio;
import com.project.investanalyst.entity.TradeType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse<executionTime>(
        Long Id,
        Portfolio portfolio,
        String ticker,
        TradeType type,
        BigDecimal quantity,
        BigDecimal executionPrice,
        LocalDateTime executionTime


){}


