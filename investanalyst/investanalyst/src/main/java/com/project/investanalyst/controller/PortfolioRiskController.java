package com.project.investanalyst.controller;
import com.project.investanalyst.dto.PortfolioRiskResponse;
import com.project.investanalyst.service.PortfolioRiskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioRiskController {
    private final PortfolioRiskService risk;
    public PortfolioRiskController(PortfolioRiskService risk) {
        this.risk = risk;
    }
    @GetMapping("/portfolio/{id}/risk")
    public PortfolioRiskResponse getRisk(@PathVariable Long id) {
        return risk.analyze(id);
    }
}
