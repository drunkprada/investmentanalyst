package com.project.investanalyst.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Development-only price policy. Client-supplied prices are NOT independently verified quotes.
 * Replace this with a trusted market-data adapter before claiming realistic trade execution.
 */
@Component
public class TradePricingPolicy {
    public BigDecimal validateDemoPrice(BigDecimal price) {
        if (price == null || price.signum() <= 0 || price.scale() > 2
                || price.precision() - price.scale() > 17) {
            throw new IllegalArgumentException("Demo execution price must be positive with at most 2 decimal places");
        }
        return price;
    }
}
