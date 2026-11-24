
package com.project.investanalyst.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
@Service
public class RiskAnalysisService {

    public List<Double> calculateDailyReturns(List<Double> prices) {
        if (prices == null || prices.size() < 2) {
            throw new IllegalArgumentException(
                    "At least two prices are required"
            );
        }

        for (Double price : prices) {
            if (price == null || !Double.isFinite(price) || price <= 0) {
                throw new IllegalArgumentException("Prices must be positive and finite");
            }
        }

        List<Double> returns = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            double previousPrice = prices.get(i - 1);
            double currentPrice = prices.get(i);

            double dailyReturn =
                    (currentPrice - previousPrice) / previousPrice;

            if (!Double.isFinite(dailyReturn)) {
                throw new IllegalArgumentException(
                        "Calculated return is not finite"
                );
            }

            returns.add(dailyReturn);
        }

        return returns;
    }

    public double calculateAnnualizedVolatility(List<Double> prices) {
        List<Double> returns = calculateDailyReturns(prices);

        if (returns.size() < 2) {
            throw new IllegalArgumentException(
                    "At least three prices are required"
            );
        }

        //mean daily return
        double mean = 0.0;

        for (double dailyReturn : returns) {
            mean += dailyReturn;
        }

        mean /= returns.size();

        // sum of squared deviations
        double squaredDifferences = 0.0;

        for (double dailyReturn : returns) {
            double difference = dailyReturn - mean;
            squaredDifferences += difference * difference;
        }

        // Sample variance uses n - 1
        double variance =
                squaredDifferences / (returns.size() - 1);

        double dailyVolatility = Math.sqrt(variance);

        // Annualize using 252 trading days
        double annualizedVolatility =
                dailyVolatility * Math.sqrt(252);

        if (!Double.isFinite(annualizedVolatility)) {
            throw new IllegalArgumentException(
                    "Calculated volatility is not finite"
            );
        }

        return annualizedVolatility;
    }

    public double calculateHistoricalVaR(
            List<Double> prices,
            double portfolioValue,
            double confidenceLevel) {

        if (!Double.isFinite(portfolioValue) || portfolioValue <= 0) {
            throw new IllegalArgumentException(
                    "Portfolio value must be positive and finite"
            );
        }

        if (!Double.isFinite(confidenceLevel)
                || confidenceLevel <= 0
                || confidenceLevel >= 1) {
            throw new IllegalArgumentException(
                    "Confidence level must be between 0 and 1"
            );
        }

        List<Double> returns = calculateDailyReturns(prices);

        // Require a minimum sample for historical estimation.
        if (returns.size() < 20) {
            throw new IllegalArgumentException(
                    "At least 20 daily returns are required"
            );
        }

        List<Double> sortedReturns = new ArrayList<>(returns);
        sortedReturns.sort(Double::compareTo);

        // Nearest-rank empirical lower-tail quantile.
        java.math.BigDecimal tailProbability =
                java.math.BigDecimal.ONE.subtract(
                        java.math.BigDecimal.valueOf(confidenceLevel)
                );

        int rank = tailProbability
                .multiply(java.math.BigDecimal.valueOf(sortedReturns.size()))
                .setScale(0, java.math.RoundingMode.CEILING)
                .intValueExact();
        int index = Math.max(0, rank - 1);

        double tailReturn = sortedReturns.get(index);

        // Report VaR as a non-negative monetary loss.
        return Math.max(0.0, -tailReturn * portfolioValue);
    }

    public Map<String, Double> calculatePortfolioWeights(
            Map<String, Double> marketValues) {

        if (marketValues == null || marketValues.isEmpty()) {
            throw new IllegalArgumentException(
                    "Portfolio must contain at least one holding"
            );
        }

        double totalValue = 0.0;

        for (Map.Entry<String, Double> entry : marketValues.entrySet()) {
            String ticker = entry.getKey();
            Double value = entry.getValue();

            if (ticker == null || ticker.isBlank()
                    || value == null
                    || !Double.isFinite(value)
                    || value <= 0) {
                throw new IllegalArgumentException(
                        "Invalid ticker or market value"
                );
            }

            totalValue += value;
        }

        if (!Double.isFinite(totalValue)) {
            throw new IllegalArgumentException(
                    "Total portfolio value is not finite"
            );
        }

        Map<String, Double> weights = new LinkedHashMap<>();

        for (Map.Entry<String, Double> entry : marketValues.entrySet()) {
            weights.put(
                    entry.getKey(),
                    entry.getValue() / totalValue
            );
        }

        return weights;
    }

    public double calculateLargestHoldingConcentration(
            Map<String, Double> marketValues) {

        Map<String, Double> weights =
                calculatePortfolioWeights(marketValues);

        double largestWeight = 0.0;

        for (double weight : weights.values()) {
            largestWeight = Math.max(largestWeight, weight);
        }

        return largestWeight;
    }


}
