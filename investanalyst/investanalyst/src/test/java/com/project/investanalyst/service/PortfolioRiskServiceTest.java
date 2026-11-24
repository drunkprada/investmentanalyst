package com.project.investanalyst.service;

import com.project.investanalyst.dto.PortfolioRiskResponse;
import com.project.investanalyst.entity.Holding;
import com.project.investanalyst.entity.Portfolio;
import com.project.investanalyst.repository.HoldingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortfolioRiskServiceTest {
    private final PortfolioService portfolios = mock(PortfolioService.class);
    private final HoldingRepository holdings = mock(HoldingRepository.class);
    private final MarketDataService market = mock(MarketDataService.class);
    private final PortfolioRiskService service =
            new PortfolioRiskService(portfolios, holdings, market, new RiskAnalysisService());

    @Test
    void alignsTradingDatesAndCalculatesPortfolioRisk() {
        Portfolio portfolio = new Portfolio("Test", new BigDecimal("1000.00"));
        when(portfolios.getPortfolio(7L)).thenReturn(portfolio);
        when(holdings.findByPortfolioId(7L)).thenReturn(List.of(
                new Holding("AAPL", BigDecimal.ONE, BigDecimal.ONE, portfolio),
                new Holding("MSFT", BigDecimal.ONE, BigDecimal.ONE, portfolio)));
        List<HistoricalPrice> a = new ArrayList<>();
        List<HistoricalPrice> b = new ArrayList<>();
        for (int i = 0; i < 22; i++) {
            LocalDate day = LocalDate.of(2026, 1, 1).plusDays(i);
            a.add(new HistoricalPrice(day, 100));
            b.add(new HistoricalPrice(day, 100));
        }
        // A non-shared date must not be included in the aligned returns.
        a.add(new HistoricalPrice(LocalDate.of(2026, 2, 1), 500));
        when(market.getHistoricalPrices("AAPL", 252)).thenReturn(a);
        when(market.getHistoricalPrices("MSFT", 252)).thenReturn(b);
        PortfolioRiskResponse result = service.analyze(7L);
        assertEquals(21, result.observations());
        assertEquals(0, result.annualizedVolatility(), 1e-10);
        assertEquals(0, result.historicalVaR95(), 1e-10);
        assertEquals(0.5, result.largestHoldingConcentration(), 1e-10);
        assertEquals(200, result.investedValue(), 1e-10);
    }

    @Test
    void rejectsEmptyPortfolio() {
        when(portfolios.getPortfolio(7L)).thenReturn(new Portfolio("Empty", BigDecimal.ZERO));
        when(holdings.findByPortfolioId(7L)).thenReturn(List.of());
        assertThrows(ResponseStatusException.class, () -> service.analyze(7L));
        verifyNoInteractions(market);
    }

    @Test
    void rejectsInsufficientSharedDates() {
        Portfolio portfolio = new Portfolio("Test", BigDecimal.ZERO);
        when(portfolios.getPortfolio(7L)).thenReturn(portfolio);
        when(holdings.findByPortfolioId(7L)).thenReturn(List.of(
                new Holding("AAPL", BigDecimal.ONE, BigDecimal.ONE, portfolio),
                new Holding("MSFT", BigDecimal.ONE, BigDecimal.ONE, portfolio)));
        List<HistoricalPrice> a = new ArrayList<>();
        List<HistoricalPrice> b = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            a.add(new HistoricalPrice(LocalDate.of(2026, 1, 1).plusDays(i), 100));
            b.add(new HistoricalPrice(LocalDate.of(2026, 2, 1).plusDays(i), 100));
        }
        when(market.getHistoricalPrices("AAPL", 252)).thenReturn(a);
        when(market.getHistoricalPrices("MSFT", 252)).thenReturn(b);
        assertThrows(ResponseStatusException.class, () -> service.analyze(7L));
    }
}
