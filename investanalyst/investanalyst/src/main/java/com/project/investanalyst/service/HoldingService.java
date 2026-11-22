package com.project.investanalyst.service;

import com.project.investanalyst.entity.Holding;
import com.project.investanalyst.entity.Portfolio;
import com.project.investanalyst.repository.HoldingRepository;
import com.project.investanalyst.repository.PortfolioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class HoldingService {
    private final HoldingRepository holdingRepository;
    private final PortfolioRepository portfolioRepository;
    public HoldingService(HoldingRepository holdingRepository, PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
    }

    public List<Holding> getHoldingsByPortfolioId(Long portfolioId){
        return holdingRepository.findByPortfolioId(portfolioId);
    }
    @Transactional
    public void buyStock(Long portfolioId, String ticker, BigDecimal quantity, BigDecimal purchasePrice){
        //check if there's enough money in portfolio for the quantity of stock
        //then we take up the quantity*singular stock price and then subtract it from the total cash

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(()->new IllegalArgumentException("Valid Portfolio Id"));
        //need to create a check method to check value
        BigDecimal spendingCash = quantity.multiply(purchasePrice);
        if(portfolio.getAvailableCash().compareTo(spendingCash) >= 0){
            //Here i subtract the cash and then create holding based on the ticker
            portfolio.deductCash(spendingCash);
            Optional<Holding> existingHolding = holdingRepository.findByPortfolioIdandTicker(portfolioId,ticker);

            if(existingHolding.isEmpty()){
                Holding holding = new Holding(ticker,purchasePrice,quantity,portfolio);
                holdingRepository.save(holding);
            }

            else{

                Holding holding = existingHolding.get();
                holding.existingBuy(purchasePrice,quantity);


            }
        }
        else if(portfolio.getAvailableCash().compareTo(spendingCash) < 0){
            throw new ResponseStatusException(
                    HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE,
                    "Insufficient Cash Balance");
        }
    }
}
