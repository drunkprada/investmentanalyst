package com.project.investanalyst.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.investanalyst.repository.HoldingRepository;
import com.project.investanalyst.repository.PortfolioRepository;
import com.project.investanalyst.repository.TradeRepository;
import com.project.investanalyst.service.HistoricalPrice;
import com.project.investanalyst.service.MarketDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PortfolioWorkflowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PortfolioRepository portfolios;
    @Autowired HoldingRepository holdings;
    @Autowired TradeRepository trades;

    @MockitoBean MarketDataService marketData;

    private long createPortfolio() throws Exception {
        String body = mvc.perform(post("/portfolio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Integration Test\",\"initialCash\":1000.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableCash").value(1000.00))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asLong();
    }

    private void buy(long id, String ticker, int quantity, int price) throws Exception {
        mvc.perform(post("/portfolio/{id}/buy", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"%s\",\"quantity\":%d,\"price\":%d}"
                                .formatted(ticker, quantity, price)))
                .andExpect(status().isNoContent());
    }

    @Test
    void tradingWorkflowPersistsStateAndRejectsInvalidTrades() throws Exception {
        long id = createPortfolio();
        buy(id, "AAPL", 2, 100);

        mvc.perform(get("/portfolio/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCash").value(800.00));
        mvc.perform(get("/portfolio/{id}/holdings", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].quantity").value(2));

        mvc.perform(post("/portfolio/{id}/buy", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"MSFT\",\"quantity\":100,\"price\":100}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/portfolio/{id}/sell", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"AAPL\",\"quantity\":3,\"price\":120}"))
                .andExpect(status().isConflict());

        // Rejected operations must leave persistent state unchanged.
        mvc.perform(get("/portfolio/{id}", id))
                .andExpect(jsonPath("$.availableCash").value(800.00));
        assertEquals(1, holdings.findByPortfolioId(id).size());
        assertEquals(0, holdings.findByPortfolioId(id).get(0)
                .getQuantity().compareTo(new java.math.BigDecimal("2")));
        assertEquals(1, trades.findByPortfolioIdOrderByExecutionTimeDesc(id).size());

        mvc.perform(post("/portfolio/{id}/sell", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"AAPL\",\"quantity\":1,\"price\":120}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/portfolio/{id}", id))
                .andExpect(jsonPath("$.availableCash").value(920.00));
        mvc.perform(get("/portfolio/{id}/holdings", id))
                .andExpect(jsonPath("$[0].quantity").value(1));
        mvc.perform(get("/portfolio/{id}/trades", id))
                .andExpect(jsonPath("$.length()").value(2));
        assertEquals(2, trades.findByPortfolioIdOrderByExecutionTimeDesc(id).size());
    }

    @Test
    void riskWorkflowUsesControlledHistoryAndRejectsInsufficientData() throws Exception {
        long id = createPortfolio();
        buy(id, "AAPL", 2, 100);
        buy(id, "MSFT", 1, 100);

        List<HistoricalPrice> stable = new ArrayList<>();
        for (int i = 0; i < 22; i++) {
            stable.add(new HistoricalPrice(LocalDate.of(2026, 1, 1).plusDays(i), 100.0));
        }
        when(marketData.getHistoricalPrices("AAPL", 252)).thenReturn(stable);
        when(marketData.getHistoricalPrices("MSFT", 252)).thenReturn(stable);

        mvc.perform(get("/portfolio/{id}/risk", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId").value(id))
                .andExpect(jsonPath("$.observations").value(21))
                .andExpect(jsonPath("$.investedValue").value(300.0))
                .andExpect(jsonPath("$.cash").value(700.0))
                .andExpect(jsonPath("$.annualizedVolatility").value(0.0))
                .andExpect(jsonPath("$.historicalVaR95").value(0.0))
                .andExpect(jsonPath("$.largestHoldingConcentration").value(2.0 / 3.0, 0.000001))
                .andExpect(jsonPath("$.holdingWeights.AAPL").value(2.0 / 3.0, 0.000001))
                .andExpect(jsonPath("$.holdingWeights.MSFT").value(1.0 / 3.0, 0.000001));

        verify(marketData).getHistoricalPrices("AAPL", 252);
        verify(marketData).getHistoricalPrices("MSFT", 252);

        when(marketData.getHistoricalPrices("MSFT", 252)).thenReturn(stable.subList(0, 5));
        mvc.perform(get("/portfolio/{id}/risk", id))
                .andExpect(status().isBadGateway());
    }
}
