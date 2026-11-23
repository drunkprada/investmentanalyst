package com.project.investanalyst.service;

import com.project.investanalyst.entity.*;
import com.project.investanalyst.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class HoldingService {
    private final HoldingRepository holdingRepository;
    private final PortfolioRepository portfolioRepository;
    private final TradeRepository tradeRepository;
    private final TradePricingPolicy pricingPolicy;

    public HoldingService(HoldingRepository holdingRepository, PortfolioRepository portfolioRepository,
                          TradeRepository tradeRepository, TradePricingPolicy pricingPolicy) {
        this.holdingRepository = holdingRepository;
        this.portfolioRepository = portfolioRepository;
        this.tradeRepository = tradeRepository;
        this.pricingPolicy = pricingPolicy;
    }

    @Transactional(readOnly = true)
    public List<Holding> getHoldingsByPortfolioId(Long portfolioId) {
        requirePortfolio(portfolioId);
        return holdingRepository.findByPortfolioId(portfolioId);
    }

    @Transactional(readOnly = true)
    public List<Trade> getTradesByPortfolioId(Long portfolioId) {
        requirePortfolio(portfolioId);
        return tradeRepository.findByPortfolioIdOrderByExecutionTimeDesc(portfolioId);
    }

    private Portfolio requirePortfolio(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    private Portfolio lockPortfolio(Long id) {
        return portfolioRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    private String normalizeTicker(String ticker) {
        if (ticker == null || !ticker.matches("[A-Za-z][A-Za-z0-9.\\-]{0,9}")) {
            throw new IllegalArgumentException("Invalid ticker");
        }
        return ticker.toUpperCase(Locale.ROOT);
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0 || quantity.scale() > 8
                || quantity.precision() - quantity.scale() > 16) {
            throw new IllegalArgumentException("Quantity must be positive with at most 8 decimal places");
        }
    }

    @Transactional
    public void buyStock(Long portfolioId, String ticker, BigDecimal quantity, BigDecimal purchasePrice) {
        validateQuantity(quantity);
        String symbol = normalizeTicker(ticker);
        BigDecimal price = pricingPolicy.validateDemoPrice(purchasePrice);
        Portfolio portfolio = lockPortfolio(portfolioId);
        BigDecimal cost = quantity.multiply(price);
        if (cost.scale() > 2) {
            throw new IllegalArgumentException("Trade total must be representable in cents");
        }
        if (portfolio.getAvailableCash().compareTo(cost) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient cash balance");
        }
        Holding holding = holdingRepository.findByPortfolioIdAndTicker(portfolioId, symbol).orElse(null);
        if (holding == null) {
            holdingRepository.save(new Holding(symbol, price, quantity, portfolio));
        } else {
            holding.existingBuy(price, quantity);
        }
        portfolio.deductCash(cost);
        tradeRepository.save(new Trade(portfolio, symbol, TradeType.BUY, quantity, price));
    }

    @Transactional
    public void sellStock(Long portfolioId, String ticker, BigDecimal quantity, BigDecimal sellPrice) {
        validateQuantity(quantity);
        String symbol = normalizeTicker(ticker);
        BigDecimal price = pricingPolicy.validateDemoPrice(sellPrice);
        Portfolio portfolio = lockPortfolio(portfolioId);
        Holding holding = holdingRepository.findByPortfolioIdAndTicker(portfolioId, symbol)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Holding not found"));
        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient shares owned");
        }
        BigDecimal proceeds = quantity.multiply(price);
        if (proceeds.scale() > 2) {
            throw new IllegalArgumentException("Trade total must be representable in cents");
        }
        if (holding.getQuantity().compareTo(quantity) == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.reduceQuantity(quantity);
        }
        portfolio.addCash(proceeds);
        tradeRepository.save(new Trade(portfolio, symbol, TradeType.SELL, quantity, price));
    }
}
