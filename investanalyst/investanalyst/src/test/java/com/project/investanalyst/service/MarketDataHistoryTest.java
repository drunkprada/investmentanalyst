package com.project.investanalyst.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MarketDataHistoryTest {
    private MarketDataService service;
    private MockRestServiceServer server;

    @BeforeEach
    void setup() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new MarketDataService(builder, "test-api-key");
    }

    @Test
    void sortsHistoricalPricesOldestFirst() {
        server.expect(requestTo("https://api.twelvedata.com/time_series?symbol=AAPL&interval=1day&outputsize=2"))
                .andExpect(header("Authorization", "apikey test-api-key"))
                .andRespond(withSuccess("""
                        {"values":[
                          {"datetime":"2026-11-23","close":"105"},
                          {"datetime":"2026-11-20","close":"100"}]}
                        """, MediaType.APPLICATION_JSON));
        var result = service.getHistoricalPrices("AAPL", 2);
        assertEquals(LocalDate.of(2026, 11, 20), result.get(0).date());
        assertEquals(105, result.get(1).close(), 1e-9);
        server.verify();
    }

    @Test
    void rejectsDuplicateDates() {
        server.expect(requestTo("https://api.twelvedata.com/time_series?symbol=AAPL&interval=1day&outputsize=2"))
                .andRespond(withSuccess("""
                        {"values":[
                          {"datetime":"2026-11-20","close":"100"},
                          {"datetime":"2026-11-20","close":"101"}]}
                        """, MediaType.APPLICATION_JSON));
        assertThrows(IllegalStateException.class, () -> service.getHistoricalPrices("AAPL", 2));
        server.verify();
    }

    @Test
    void rejectsBadClosingPrice() {
        server.expect(requestTo("https://api.twelvedata.com/time_series?symbol=AAPL&interval=1day&outputsize=2"))
                .andRespond(withSuccess("""
                        {"values":[{"datetime":"2026-11-20","close":"-2"}]}
                        """, MediaType.APPLICATION_JSON));
        assertThrows(IllegalStateException.class, () -> service.getHistoricalPrices("AAPL", 2));
        server.verify();
    }

    @Test
    void rejectsProviderError() {
        server.expect(requestTo("https://api.twelvedata.com/time_series?symbol=AAPL&interval=1day&outputsize=2"))
                .andRespond(withServerError());
        assertThrows(IllegalStateException.class, () -> service.getHistoricalPrices("AAPL", 2));
        server.verify();
    }

    @Test
    void rejectsInvalidRequestWithoutNetwork() {
        assertThrows(IllegalArgumentException.class, () -> service.getHistoricalPrices("AAPL!", 20));
        assertThrows(IllegalArgumentException.class, () -> service.getHistoricalPrices("AAPL", 1));
        server.verify();
    }
}
