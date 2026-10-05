package com.project.portfolio.domain;
public class Order{
    private final String orderId;
    private final String portfolioId;
    private final String ticker;
    private final Side side;
    private final int quantity;
    private OrderStatus status;

    public Order(String orderId, String portfolioId, String ticker, Side side, int quantity){
        
        if(side == null){
            throw new IllegalArgumentException("Side cannot be null");
        }
        if(orderId == null || orderId.isBlank()){
            throw new IllegalArgumentException("Order Id cannot be blank");
        }
        
        if(portfolioId == null || portfolioId.isBlank()){
            throw new IllegalArgumentException("Portfolio Id cannot be blank");
        }
        
        if(quantity<=0){
            throw new IllegalStateException("Quanity of tickers cannot be 0");
        }
        if(ticker == null || ticker.isBlank()){
            throw new IllegalStateException("Ticker cannot be blank");
        }
        this.orderId = orderId;
        this.portfolioId = portfolioId;
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
        this.status = OrderStatus.PENDING;
    }

    public String getOrderId(){
        return orderId;
    }
    public String getPortfolioId(){
        return portfolioId;
    }
    public String getTicker(){
        return ticker;
    }
    public Side getSide(){
        return side;
    }
    public int getQuantity(){
        return quantity;
    }
    public OrderStatus getOrderStatus(){
        return status;
    }
    public void markFilled() {
    // Reject unless the current status is PENDING.
    // Otherwise, change status to FILLED.
    if(status != OrderStatus.PENDING){
        throw new IllegalArgumentException("Status must be pending");
    }
    else{
        status = OrderStatus.FILLED;
    }
}

    public void markRejected() {
    if(status != OrderStatus.PENDING){
        throw new IllegalArgumentException("Status must be pending");
    }
    else{
        status = OrderStatus.REJECTED;
    }
}

}