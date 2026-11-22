package com.project.investanalyst.controller;

import com.project.investanalyst.dto.PortfolioResponse;
import com.project.investanalyst.entity.Portfolio;
import com.project.investanalyst.service.PortfolioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/portfolio/{id}")
    public PortfolioResponse getPortfolio(@PathVariable Long id) {
        return toResponse(portfolioService.getPortfolio(id));
    }

    @PostMapping("/portfolio")
    public PortfolioResponse createPortfolio(@RequestBody Portfolio portfolio) {
        return toResponse(portfolioService.createPortfolio(
                portfolio.getName(), portfolio.getInitialCash()
        ));
    }

    @GetMapping("/portfolio")
    public List<PortfolioResponse> getAllPortfolios() {
        return portfolioService.getAllPortfolios().stream()
                .map(this::toResponse)
                .toList();
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getInitialCash(),
                portfolio.getAvailableCash(),
                portfolio.getCurrency()
        );
    }
}
