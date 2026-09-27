
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
class Portfolio{
    private final String portfolioId;
    private final String name;
    private BigDecimal cashBalance;
    private final Map<String, Holding> holdings = new HashMap<>();
    
    private BigDecimal realisedPnl = BigDecimal.ZERO;
    public Portfolio(String portfolioId, String name, BigDecimal cashBalance){
        if (cashBalance == null || cashBalance.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Starting cash balance cannot be null or negative");
    }
        this.name = name;
        this.portfolioId = portfolioId;
        this.cashBalance = cashBalance;
    }
    public BigDecimal getRealisedPnl(){
        return realisedPnl; 
    }
    public String getName(){
        return name;
    }
    public String getPortfolioId(){
        return portfolioId;
    }
    public BigDecimal getCashBalance(){
        return cashBalance;
    }

    public Holding getHolding(String ticker){
        return holdings.get(ticker);

    }
    public void buy(String ticker, int quantity, BigDecimal purchasePrice){
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker cannot be blank");
        }

        if (quantity <= 0) {
        throw new IllegalArgumentException("Additional quantity must be positive");
    }

        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be positive");
        }
        
        
        BigDecimal totalCost = purchasePrice.multiply(BigDecimal.valueOf(quantity));
        if (totalCost.compareTo(cashBalance) > 0) {
            throw new IllegalArgumentException("Insufficient cash");
    }
        
        Holding holding = getHolding(ticker);

        if (holding == null) {
            holdings.put(ticker, new Holding(ticker, quantity, purchasePrice));
        } else {
            holding.addShares(quantity, purchasePrice);
    }
        cashBalance = cashBalance.subtract(totalCost);
    }
    public void sell(String ticker, int quantity, BigDecimal salePrice) {   
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker cannot be blank");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Sale price must be positive");
        }

        Holding holding = getHolding(ticker);

        if (holding == null) {
            throw new IllegalArgumentException(
                    "Portfolio does not own " + ticker
            );
        }

        if (quantity > holding.getQuantity()) {
            throw new IllegalArgumentException(
                    "Cannot sell more shares than are owned"
            );
        }

        BigDecimal saleProceeds =
                salePrice.multiply(BigDecimal.valueOf(quantity));

        BigDecimal originalCost =
                holding.getAveragePurchasePrice()
                        .multiply(BigDecimal.valueOf(quantity));

        BigDecimal profitOrLoss = saleProceeds.subtract(originalCost);

        holding.removeShares(quantity);

        cashBalance = cashBalance.add(saleProceeds);
        realisedPnl = realisedPnl.add(profitOrLoss);

        if (holding.getQuantity() == 0) {
            holdings.remove(ticker);
        }
    }
}

