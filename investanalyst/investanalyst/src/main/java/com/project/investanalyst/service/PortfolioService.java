package com.project.investanalyst.service;

import com.project.investanalyst.entity.Portfolio;

import com.project.investanalyst.repository.PortfolioRepository;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public Portfolio createPortfolio(String name, BigDecimal initialCash) {
        Portfolio portfolio = new Portfolio(name, initialCash);
        return portfolioRepository.save(portfolio);
    }
    public Portfolio getPortfolio(Long id) {
        return portfolioRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Portfolio not found"
        ));
    }
    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }

}
