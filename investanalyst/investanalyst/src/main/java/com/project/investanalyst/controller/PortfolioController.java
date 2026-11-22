    package com.project.investanalyst.controller;

    import com.project.investanalyst.entity.Portfolio;
    import com.project.investanalyst.repository.PortfolioRepository;
    import com.project.investanalyst.service.PortfolioService;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Optional;

    @RestController

    public class PortfolioController {
        private final PortfolioService portfolioService;

        public PortfolioController(PortfolioService portfolioService) {
            this.portfolioService = portfolioService;

        }
        @GetMapping("/portfolio/{id}")
        public Portfolio getPortfolio(@PathVariable Long id) {
            return portfolioService.getPortfolio(id);
        }

        @PostMapping("/portfolio")
        public Portfolio createPortfolio(@RequestBody Portfolio portfolio){
            return portfolioService.createPortfolio(
                    portfolio.getName(),
                    portfolio.getInitialCash()
            );
        }
        @GetMapping("/portfolio")
        public List<Portfolio> getAllPortfolios() {
            return portfolioService.getAllPortfolios();
        }
    }
