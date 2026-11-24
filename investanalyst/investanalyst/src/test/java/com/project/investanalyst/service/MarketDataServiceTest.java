
package com.project.investanalyst.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MarketDataServiceTest {

    private MarketDataService service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        service = new MarketDataService(builder, "test-api-key");
    }

    @Test
    void retrievesValidPrice() {
        server.expect(requestTo(
                        "https://api.twelvedata.com/price?symbol=AAPL"))
                .andExpect(header(
                        "Authorization", "apikey test-api-key"))
                .andRespond(withSuccess(
                        "{\"price\":\"200.50\"}",
                        MediaType.APPLICATION_JSON));

        assertEquals(200.50, service.getLatestPrice("AAPL"), 1e-9);
        server.verify();
    }

    @Test
    void rejectsInvalidTicker() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getLatestPrice("AAPL!"));
        server.verify();
    }

    @Test
    void rejectsZeroPrice() {
        server.expect(requestTo(
                        "https://api.twelvedata.com/price?symbol=AAPL"))
                .andRespond(withSuccess(
                        "{\"price\":\"0\"}",
                        MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> service.getLatestPrice("AAPL"));
        server.verify();
    }

    @Test
    void rejectsMissingPrice() {
        server.expect(requestTo(
                        "https://api.twelvedata.com/price?symbol=AAPL"))
                .andRespond(withSuccess(
                        "{\"status\":\"error\",\"message\":\"Invalid symbol\"}",
                        MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> service.getLatestPrice("AAPL"));
        server.verify();
    }

    @Test
    void handlesProviderFailure() {
        server.expect(requestTo(
                        "https://api.twelvedata.com/price?symbol=AAPL"))
                .andRespond(withServerError());

        assertThrows(IllegalStateException.class,
                () -> service.getLatestPrice("AAPL"));
        server.verify();
    }
}
