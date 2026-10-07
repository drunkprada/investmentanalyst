package com.project.portfolio;

import com.project.portfolio.domain.*;
import com.project.portfolio.service.OrderService;
import java.math.BigDecimal;

/** Standalone regression check: runs without Spring or external services. */
public class QuantityOverflowRegression {
    public static void main(String[] args) {
        Portfolio portfolio = new Portfolio("p1", "Test", new BigDecimal("3000000000"));
        portfolio.buy("TEST", Integer.MAX_VALUE, BigDecimal.ONE);
        BigDecimal cashBefore = portfolio.getCashBalance();
        Order order = new Order("o1", "p1", "TEST", Side.BUY, 1);
        try {
            new OrderService().executeOrder(order, portfolio, BigDecimal.ONE);
            throw new AssertionError("Overflowing order must be rejected");
        } catch (IllegalArgumentException expected) {
            if (order.getOrderStatus() != OrderStatus.REJECTED
                    || portfolio.getHolding("TEST").getQuantity() != Integer.MAX_VALUE
                    || !portfolio.getCashBalance().equals(cashBefore)
                    || !portfolio.getHolding("TEST").getAveragePurchasePrice().equals(BigDecimal.ONE)) {
                throw new AssertionError("Rejected order changed portfolio state");
            }
        }
        Holding normal = new Holding("TEST", 2, new BigDecimal("10"));
        normal.addShares(2, new BigDecimal("20"));
        if (normal.getQuantity() != 4
                || normal.getAveragePurchasePrice().compareTo(new BigDecimal("15")) != 0) {
            throw new AssertionError("Normal weighted average changed");
        }
        System.out.println("Quantity overflow and normal purchase checks passed");
    }
}
