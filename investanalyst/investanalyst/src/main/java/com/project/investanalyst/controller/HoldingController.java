package com.project.investanalyst.controller;

import com.project.investanalyst.entity.Holding;
import com.project.investanalyst.service.HoldingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HoldingController {
    private final HoldingService holdingService;
    public HoldingController(HoldingService holdingService){
        this.holdingService = holdingService;
    }
    @GetMapping("/portfolio/{id}/holdings")
    public List<Holding> getHoldings(@PathVariable Long id){
        return holdingService.getHoldingsByPortfolioId(id);

    }
}
