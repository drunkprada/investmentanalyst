
package com.project.investanalyst.service;

import com.project.investanalyst.entity.Holding;
import com.project.investanalyst.entity.Portfolio;
import com.project.investanalyst.entity.Trade;
import com.project.investanalyst.entity.TradeType;
import com.project.investanalyst.repository.HoldingRepository;
import com.project.investanalyst.repository.PortfolioRepository;

import com.project.investanalyst.repository.TradeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final PortfolioRepository portfolioRepository;
    private final TradeRepository tradeRepository;
    public HoldingService(
            HoldingRepository holdingRepository,
            PortfolioRepository portfolioRepository, TradeRepository tradeRepository) {
        this.holdingRepository = holdingRepository;
        this.portfolioRepository = portfolioRepository;
        this.tradeRepository = tradeRepository;
    }

    public List<Holding> getHoldingsByPortfolioId(Long portfolioId) {
        return holdingRepository.findByPortfolioId(portfolioId);
    }

    @Transactional
    public void buyStock(
            Long portfolioId,
            String ticker,
            BigDecimal quantity,
            BigDecimal purchasePrice) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Portfolio not found"
                ));

        String normalizedTicker = ticker.trim().toUpperCase(Locale.ROOT);

        BigDecimal spendingCash = quantity.multiply(purchasePrice);

        if (portfolio.getAvailableCash().compareTo(spendingCash) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient cash balance"
            );
        }

        Optional<Holding> existingHolding = holdingRepository.findByPortfolioIdAndTicker(
                        portfolioId,
                        normalizedTicker
                );

        if (existingHolding.isPresent()) {
            existingHolding.get().existingBuy(purchasePrice, quantity);
        } else {
            Holding holding = new Holding(
                    normalizedTicker,
                    purchasePrice,
                    quantity,
                    portfolio
            );

            holdingRepository.save(holding);
        }

        portfolio.deductCash(spendingCash);

        Trade trade = new Trade(portfolio,normalizedTicker, TradeType.BUY,quantity, purchasePrice);
        tradeRepository.save(trade);



    }

    @Transactional
    public void sellStock(Long portfolioId, String ticker, BigDecimal quantity, BigDecimal sellPrice){
        //Portfolio Validity check
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));

        //Checking shares to sell is less than present shares -> already done in repo
        String normalizedTicker = ticker.trim().toUpperCase(Locale.ROOT);
        Holding holding = holdingRepository.findByPortfolioIdAndTicker(portfolioId, normalizedTicker).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Holding not found"));
        if(holding.getQuantity().compareTo(quantity)<0){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient shares owned");
        }
        BigDecimal salePrice = quantity.multiply(sellPrice);
        if (holding.getQuantity().compareTo(quantity) == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.reduceQuantity(quantity);
        }
        portfolio.addCash(salePrice);

        Trade trade = new Trade(portfolio,normalizedTicker, TradeType.SELL,quantity, sellPrice);
        tradeRepository.save(trade);
    }

    public List<Trade> getTradesByPortfolioId(Long portfolioId){
        return tradeRepository.findByPortfolioIdOrderByExecutionTimeDesc(portfolioId);
    }
}
