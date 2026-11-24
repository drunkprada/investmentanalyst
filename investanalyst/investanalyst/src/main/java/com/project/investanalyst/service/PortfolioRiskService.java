package com.project.investanalyst.service;

import com.project.investanalyst.dto.PortfolioRiskResponse;
import com.project.investanalyst.entity.Holding;
import com.project.investanalyst.entity.Portfolio;
import com.project.investanalyst.repository.HoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PortfolioRiskService {
    private static final int HISTORY_SIZE = 252;
    private static final double CONFIDENCE = 0.95;
    private final PortfolioService portfolios;
    private final HoldingRepository holdings;
    private final MarketDataService marketData;
    private final RiskAnalysisService risk;

    public PortfolioRiskService(PortfolioService portfolios, HoldingRepository holdings,
                                MarketDataService marketData, RiskAnalysisService risk) {
        this.portfolios = portfolios;
        this.holdings = holdings;
        this.marketData = marketData;
        this.risk = risk;
    }

    public PortfolioRiskResponse analyze(Long portfolioId) {
        Portfolio portfolio = portfolios.getPortfolio(portfolioId);
        List<Holding> positions = holdings.findByPortfolioId(portfolioId);
        if (positions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Portfolio has no invested holdings");
        }

        Map<String, Map<LocalDate, Double>> series = new HashMap<>();
        Map<String, Double> values = new HashMap<>();
        Set<LocalDate> commonDates = null;

        try {
            for (Holding holding : positions) {
                String ticker = holding.getTicker();
                List<HistoricalPrice> history = marketData.getHistoricalPrices(ticker, HISTORY_SIZE);
                if (history.size() < 21) {
                    throw new IllegalStateException("Insufficient price history for " + ticker);
                }
                Map<LocalDate, Double> dated = new HashMap<>();
                for (HistoricalPrice point : history) {
                    if (dated.putIfAbsent(point.date(), point.close()) != null
                            || !Double.isFinite(point.close()) || point.close() <= 0) {
                        throw new IllegalStateException("Invalid historical series for " + ticker);
                    }
                }
                series.put(ticker, dated);
                if (commonDates == null) commonDates = new HashSet<>(dated.keySet());
                else commonDates.retainAll(dated.keySet());


            }
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Historical market data unavailable: " + ex.getMessage(), ex);
        }

        if (commonDates == null || commonDates.size() < 21) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "At least 21 shared trading dates are required");
        }
        List<LocalDate> dates = new ArrayList<>(new TreeSet<>(commonDates));
        // Value all holdings at the same most recent shared trading date.
        LocalDate valuationDate = dates.get(dates.size() - 1);
        for (Holding holding : positions) {
            double close = series.get(holding.getTicker()).get(valuationDate);
            double value = holding.getQuantity()
                    .multiply(BigDecimal.valueOf(close)).doubleValue();
            if (!Double.isFinite(value) || value <= 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Invalid holding valuation");
            }
            values.merge(holding.getTicker(), value, Double::sum);
        }
        Map<String, Double> weights = risk.calculatePortfolioWeights(values);
        double invested = values.values().stream().mapToDouble(Double::doubleValue).sum();
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < dates.size(); i++) {
            LocalDate previous = dates.get(i - 1);
            LocalDate current = dates.get(i);
            double combined = 0;
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                Map<LocalDate, Double> observations = series.get(entry.getKey());
                double prior = observations.get(previous);
                double next = observations.get(current);
                combined += entry.getValue() * ((next - prior) / prior);
            }
            if (!Double.isFinite(combined)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Non-finite portfolio return");
            }
            returns.add(combined);
        }
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        double sumSquares = 0;
        for (double daily : returns) {
            double diff = daily - mean;
            sumSquares += diff * diff;
        }
        double volatility = Math.sqrt(sumSquares / (returns.size() - 1)) * Math.sqrt(252);
        List<Double> ordered = new ArrayList<>(returns);
        ordered.sort(Double::compareTo);
        int rank = BigDecimal.ONE.subtract(BigDecimal.valueOf(CONFIDENCE))
                .multiply(BigDecimal.valueOf(ordered.size()))
                .setScale(0, RoundingMode.CEILING).intValueExact();
        double var = Math.max(0, -ordered.get(Math.max(0, rank - 1)) * invested);
        if (!Double.isFinite(volatility) || !Double.isFinite(var)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Invalid portfolio risk estimate");
        }
        return new PortfolioRiskResponse(portfolioId, portfolio.getCurrency(), returns.size(),
                dates.get(0), dates.get(dates.size() - 1), invested,
                portfolio.getAvailableCash().doubleValue(), volatility, var,
                risk.calculateLargestHoldingConcentration(values), Map.copyOf(weights));
    }
}
