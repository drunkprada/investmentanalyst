import java.math.BigDecimal;
import java.math.RoundingMode;

public class Holding {
    private final String ticker;
    private int quantity;
    private BigDecimal averagePurchasePrice;
    
    public Holding(String ticker, int quantity, BigDecimal averagePurchasePrice){
        this.ticker = ticker;
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice;
        if(quantity<=0){
            throw new IllegalArgumentException("Quanity of tickers cannot be 0");
        }
        if(averagePurchasePrice == null ||averagePurchasePrice.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Purchase Price cannot be null or negative");
        }

    }
    public String getTicker(){
        return ticker;
    }
    public int getQuantity(){
        return quantity;
    }
    public BigDecimal getAveragePurchasePrice(){
        return averagePurchasePrice;
    }


    public void addShares(int additionalQuantity, BigDecimal purchasePrice) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }

        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be positive");
        }

        BigDecimal oldCost =
            averagePurchasePrice.multiply(BigDecimal.valueOf(quantity));

        BigDecimal newCost =
            purchasePrice.multiply(BigDecimal.valueOf(additionalQuantity));

        BigDecimal totalCost = oldCost.add(newCost);
        int newQuantity = quantity + additionalQuantity;

        BigDecimal newAverage = totalCost.divide(
            BigDecimal.valueOf(newQuantity),
            8,
            RoundingMode.HALF_UP
        );

        this.quantity = newQuantity;
        this.averagePurchasePrice = newAverage;
    }
    public void removeShares(int quantityToRemove){
        if(quantityToRemove<=0){
            throw new IllegalArgumentException("Quantity must be more than 0");

        }
        if(quantityToRemove>quantity){
            throw new IllegalArgumentException("Quantity of shares in holdings lesser than sell quantity");
        }
        quantity = quantity-quantityToRemove;
    }
}
