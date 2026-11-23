
package com.project.investanalyst.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TradeRequest(
        @NotBlank String ticker,
        @NotNull @Positive BigDecimal quantity,
        @NotNull @Positive BigDecimal price
) {}
