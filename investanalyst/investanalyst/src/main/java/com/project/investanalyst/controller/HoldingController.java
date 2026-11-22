
package com.project.investanalyst.controller;


import com.project.investanalyst.dto.TradeRequest;
import com.project.investanalyst.entity.Holding;
import com.project.investanalyst.entity.Trade;
import com.project.investanalyst.service.HoldingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping("/portfolio/{id}/holdings")
    public List<Holding> getHoldings(@PathVariable Long id) {
        return holdingService.getHoldingsByPortfolioId(id);
    }

    @PostMapping("/portfolio/{id}/buy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void buyStock(@PathVariable Long id, @Valid @RequestBody TradeRequest request) {
        holdingService.buyStock(id, request.ticker(), request.quantity(), request.price());
    }

    @PostMapping("/portfolio/{id}/sell")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sellStock(@PathVariable Long id, @Valid @RequestBody TradeRequest request){
        holdingService.sellStock(id, request.ticker(), request.quantity(),request.price());
    }

    @GetMapping("/portfolio/{id}/trade")
    public List<Trade> getTrades(@PathVariable Long id){
        List<Trade> trades = holdingService.getTradesByPortfolioId(id);
        return holdingService.getTradesByPortfolioId(id);
    }
}
