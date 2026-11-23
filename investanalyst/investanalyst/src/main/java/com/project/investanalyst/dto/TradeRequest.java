package com.project.investanalyst.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record TradeRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z][A-Za-z0-9.\\-]{0,9}") String ticker,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 16, fraction = 8) BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 17, fraction = 2) BigDecimal price
) {}
