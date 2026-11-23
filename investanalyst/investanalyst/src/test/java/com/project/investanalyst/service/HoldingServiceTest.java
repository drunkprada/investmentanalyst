package com.project.investanalyst.service;

import com.project.investanalyst.entity.*;
import com.project.investanalyst.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HoldingServiceTest {
    private HoldingRepository holdings;
    private PortfolioRepository portfolios;
    private TradeRepository trades;
    private HoldingService service;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        holdings = mock(HoldingRepository.class);
        portfolios = mock(PortfolioRepository.class);
        trades = mock(TradeRepository.class);
        service = new HoldingService(holdings, portfolios, trades, new TradePricingPolicy());
        portfolio = new Portfolio("Test", new BigDecimal("1000.00"));
        when(portfolios.findByIdForUpdate(1L)).thenReturn(Optional.of(portfolio));
    }

    @Test
    void buyCreatesHoldingAndTrade() {
        when(holdings.findByPortfolioIdAndTicker(1L, "AAPL")).thenReturn(Optional.empty());
        service.buyStock(1L, "aapl", new BigDecimal("2"), new BigDecimal("25.00"));

        assertEquals(0, new BigDecimal("950.00").compareTo(portfolio.getAvailableCash()));
        ArgumentCaptor<Holding> holding = ArgumentCaptor.forClass(Holding.class);
        verify(holdings).save(holding.capture());
        assertEquals("AAPL", holding.getValue().getTicker());
        assertEquals(0, new BigDecimal("2").compareTo(holding.getValue().getQuantity()));
        ArgumentCaptor<Trade> trade = ArgumentCaptor.forClass(Trade.class);
        verify(trades).save(trade.capture());
        assertEquals(TradeType.BUY, trade.getValue().getType());
    }

    @Test
    void buyRejectsInsufficientCashWithoutMutation() {
        service.buyStock(1L, "AAPL", new BigDecimal("1"), new BigDecimal("100.00"));
        assertEquals(0, new BigDecimal("900.00").compareTo(portfolio.getAvailableCash()));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.buyStock(1L, "AAPL", new BigDecimal("20"), new BigDecimal("100.00")));
        assertEquals(0, new BigDecimal("900.00").compareTo(portfolio.getAvailableCash()));
        verify(holdings, times(1)).save(any(Holding.class));
        verify(trades, times(1)).save(any(Trade.class));
    }

    @Test
    void sellPartialKeepsHoldingAndAddsCash() {
        Holding holding = new Holding("AAPL", new BigDecimal("20.00"), new BigDecimal("5"), portfolio);
        when(holdings.findByPortfolioIdAndTicker(1L, "AAPL")).thenReturn(Optional.of(holding));
        service.sellStock(1L, "AAPL", new BigDecimal("2"), new BigDecimal("30.00"));
        assertEquals(0, new BigDecimal("3").compareTo(holding.getQuantity()));
        assertEquals(0, new BigDecimal("1060.00").compareTo(portfolio.getAvailableCash()));
        verify(holdings, never()).delete(any(Holding.class));
        verify(trades).save(any(Trade.class));
    }

    @Test
    void sellAllDeletesHolding() {
        Holding holding = new Holding("AAPL", new BigDecimal("20.00"), new BigDecimal("5"), portfolio);
        when(holdings.findByPortfolioIdAndTicker(1L, "AAPL")).thenReturn(Optional.of(holding));
        service.sellStock(1L, "AAPL", new BigDecimal("5"), new BigDecimal("30.00"));
        verify(holdings).delete(holding);
        assertEquals(0, new BigDecimal("1150.00").compareTo(portfolio.getAvailableCash()));
    }

    @Test
    void invalidQuantityAndFractionalCentTotalDoNotWrite() {
        assertThrows(IllegalArgumentException.class,
                () -> service.buyStock(1L, "AAPL", BigDecimal.ZERO, new BigDecimal("10.00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.buyStock(1L, "AAPL", new BigDecimal("0.0001"), new BigDecimal("10.00")));
        verify(trades, never()).save(any(Trade.class));
        verify(holdings, never()).save(any(Holding.class));
    }
}
