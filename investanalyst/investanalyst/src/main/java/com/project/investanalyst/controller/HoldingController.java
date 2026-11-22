package com.project.investanalyst.controller;

import com.project.investanalyst.dto.HoldingResponse;
import com.project.investanalyst.dto.TradeRequest;
import com.project.investanalyst.dto.TradeResponse;
import com.project.investanalyst.service.HoldingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class HoldingController {
    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping("/portfolio/{id}/holdings")
    public List<HoldingResponse> getHoldings(@PathVariable Long id) {
        return holdingService.getHoldingsByPortfolioId(id).stream()
                .map(holding -> new HoldingResponse(
                        holding.getId(),
                        holding.getPortfolio().getId(),
                        holding.getTicker(),
                        holding.getAverageCost(),
                        holding.getQuantity()
                ))
                .toList();
    }

    @PostMapping("/portfolio/{id}/buy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void buyStock(@PathVariable Long id, @Valid @RequestBody TradeRequest request) {
        holdingService.buyStock(id, request.ticker(), request.quantity(), request.price());
    }

    @PostMapping("/portfolio/{id}/sell")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sellStock(@PathVariable Long id, @Valid @RequestBody TradeRequest request) {
        holdingService.sellStock(id, request.ticker(), request.quantity(), request.price());
    }

    @GetMapping("/portfolio/{id}/trades")
    public List<TradeResponse> getTrades(@PathVariable Long id) {
        return holdingService.getTradesByPortfolioId(id).stream()
                .map(trade -> new TradeResponse(
                        trade.getId(),
                        trade.getPortfolio().getId(),
                        trade.getTicker(),
                        trade.getType(),
                        trade.getQuantity(),
                        trade.getExecutionPrice(),
                        trade.getExecutionTime()
                ))
                .toList();
    }
}
