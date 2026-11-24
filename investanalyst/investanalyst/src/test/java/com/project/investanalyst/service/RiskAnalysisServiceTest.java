
package com.project.investanalyst.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RiskAnalysisServiceTest {

    private RiskAnalysisService riskService;

    @BeforeEach
    void setUp() {
        riskService = new RiskAnalysisService();
    }

    // ==========================================
    // DAILY RETURNS TESTS
    // ==========================================

    @Test
    void calculatesDailyReturnsCorrectly() {
        List<Double> prices = List.of(100.0, 105.0, 102.90);

        List<Double> returns =
                riskService.calculateDailyReturns(prices);

        assertEquals(2, returns.size());
        assertEquals(0.05, returns.get(0), 1e-9);
        assertEquals(-0.02, returns.get(1), 1e-9);
    }

    @Test
    void rejectsInsufficientPriceHistory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateDailyReturns(
                        List.of(100.0)
                )
        );
    }

    @Test
    void rejectsEmptyPriceHistory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateDailyReturns(List.of())
        );
    }

    @Test
    void rejectsNullPriceHistory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateDailyReturns(null)
        );
    }

    @Test
    void rejectsZeroPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateDailyReturns(
                        List.of(100.0, 0.0)
                )
        );
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateDailyReturns(
                        List.of(100.0, -5.0)
                )
        );
    }

    @Test
    void rejectsNonFinitePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateDailyReturns(
                        List.of(100.0, Double.NaN)
                )
        );
    }

    @Test
    void unchangedPriceProducesZeroReturn() {
        List<Double> returns =
                riskService.calculateDailyReturns(
                        List.of(100.0, 100.0)
                );

        assertEquals(0.0, returns.get(0), 1e-9);
    }

    // ==========================================
    // ANNUALIZED VOLATILITY TESTS
    // ==========================================

    @Test
    void calculatesAnnualizedVolatilityCorrectly() {
        List<Double> prices = List.of(100.0, 110.0, 99.0);

        double volatility =
                riskService.calculateAnnualizedVolatility(prices);

        // Daily returns: +10% and -10%
        // Mean return: 0
        // Sample variance: 0.02
        // Annualized variance: 0.02 * 252
        double expected = Math.sqrt(5.04);

        assertEquals(expected, volatility, 1e-9);
    }

    @Test
    void constantPricesProduceZeroVolatility() {
        List<Double> prices = List.of(100.0, 100.0, 100.0);

        double volatility =
                riskService.calculateAnnualizedVolatility(prices);

        assertEquals(0.0, volatility, 1e-9);
    }

    @Test
    void rejectsInsufficientHistoryForVolatility() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateAnnualizedVolatility(
                        List.of(100.0, 105.0)
                )
        );
    }

    @Test
    void rejectsInvalidPricesForVolatility() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateAnnualizedVolatility(
                        List.of(100.0, -105.0, 110.0)
                )
        );
    }

    // ==========================================
    // PORTFOLIO WEIGHT TESTS
    // ==========================================

    @Test
    void calculatesPortfolioWeightsCorrectly() {
        Map<String, Double> holdings = Map.of(
                "AAPL", 600.0,
                "MSFT", 400.0
        );

        Map<String, Double> weights =
                riskService.calculatePortfolioWeights(holdings);

        assertEquals(0.6, weights.get("AAPL"), 1e-9);
        assertEquals(0.4, weights.get("MSFT"), 1e-9);
    }

    @Test
    void portfolioWeightsSumToOne() {
        Map<String, Double> holdings = Map.of(
                "AAPL", 300.0,
                "MSFT", 200.0,
                "GOOGL", 500.0
        );

        Map<String, Double> weights =
                riskService.calculatePortfolioWeights(holdings);

        double totalWeight = weights.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        assertEquals(1.0, totalWeight, 1e-9);
    }

    // ==========================================
    // CONCENTRATION TESTS
    // ==========================================

    @Test
    void calculatesLargestHoldingConcentration() {
        Map<String, Double> holdings = Map.of(
                "AAPL", 600.0,
                "MSFT", 400.0
        );

        double concentration =
                riskService.calculateLargestHoldingConcentration(holdings);

        assertEquals(0.6, concentration, 1e-9);
    }

    @Test
    void singleHoldingHasFullConcentration() {
        double concentration =
                riskService.calculateLargestHoldingConcentration(
                        Map.of("AAPL", 1000.0)
                );

        assertEquals(1.0, concentration, 1e-9);
    }

    // ==========================================
    // INVALID PORTFOLIO TESTS
    // ==========================================

    @Test
    void rejectsEmptyPortfolio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculatePortfolioWeights(
                        Map.of()
                )
        );
    }

    @Test
    void rejectsNegativeMarketValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculatePortfolioWeights(
                        Map.of("AAPL", -100.0)
                )
        );
    }

    @Test
    void rejectsZeroMarketValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculatePortfolioWeights(
                        Map.of("AAPL", 0.0)
                )
        );
    }

    @Test
    void rejectsNonFiniteMarketValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculatePortfolioWeights(
                        Map.of("AAPL", Double.POSITIVE_INFINITY)
                )
        );
    }


    @Test
    void calculatesHistoricalVaRCorrectly() {
        // Construct 20 returns: one -10% loss
        // followed by nineteen zero-return days.
        List<Double> prices = new java.util.ArrayList<>();
        prices.add(100.0);
        prices.add(90.0);

        for (int i = 0; i < 19; i++) {
            prices.add(90.0);
        }

        double var = riskService.calculateHistoricalVaR(
                prices, 10000.0, 0.95
        );

        // 5th percentile of 20 returns = -10%.
        assertEquals(1000.0, var, 1e-9);
    }

    @Test
    void zeroReturnsProduceZeroVaR() {
        List<Double> prices =
                java.util.Collections.nCopies(21, 100.0);

        double var = riskService.calculateHistoricalVaR(
                prices, 10000.0, 0.95
        );

        assertEquals(0.0, var, 1e-9);
    }

    @Test
    void rejectsInsufficientHistoryForVaR() {
        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateHistoricalVaR(
                        List.of(100.0, 95.0, 90.0),
                        10000.0,
                        0.95
                )
        );
    }

    @Test
    void rejectsInvalidPortfolioValueForVaR() {
        List<Double> prices =
                java.util.Collections.nCopies(21, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateHistoricalVaR(
                        prices, -1000.0, 0.95
                )
        );
    }

    @Test
    void rejectsInvalidConfidenceLevel() {
        List<Double> prices =
                java.util.Collections.nCopies(21, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> riskService.calculateHistoricalVaR(
                        prices, 10000.0, 1.0
                )
        );
    }
}