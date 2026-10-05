package com.project.portfolio.service;
import com.project.portfolio.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderService {
    
    public Trade executeOrder(Order order, Portfolio portfolio, BigDecimal executionPrice) {
        
        if(order == null || portfolio == null){
            throw new IllegalArgumentException("Cannot accept null order or portfolio");
        }
        if(executionPrice == null ||executionPrice.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Cannot accept negative execution Price");

        }

        if(!(order.getPortfolioId().equals(portfolio.getPortfolioId()))){
            throw new IllegalArgumentException("Portfolio ID does not match");

        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be executed");
        } 
        
        try{
            if (order.getSide() == Side.BUY) {
                portfolio.buy(order.getTicker(),order.getQuantity(),executionPrice);
                }
            else if(order.getSide() == Side.SELL){
                portfolio.sell(order.getTicker(),order.getQuantity(),executionPrice);
            }
    }
        catch(IllegalArgumentException e){
            order.markRejected();
            throw e;
        }
        Trade trade = new Trade(UUID.randomUUID().toString(),order.getOrderId(),order.getPortfolioId(),order.getTicker(),order.getSide(), order.getQuantity(),executionPrice,Instant.now());
        order.markFilled();
        return trade;
    }
}