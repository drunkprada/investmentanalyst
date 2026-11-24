
package com.project.investanalyst.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.regex.Pattern;

@Service
public class MarketDataService {

    private static final Pattern TICKER_PATTERN =
            Pattern.compile("[A-Z]{1,5}");

    private final RestClient client;
    private final String apiKey;

    public MarketDataService(
            RestClient.Builder builder,
            @Value("${market-data.api-key}") String apiKey) {

        this.client = builder
                .baseUrl("https://api.twelvedata.com")
                .build();

        this.apiKey = apiKey;
    }

    public double getLatestPrice(String ticker) {
        if (ticker == null ||
                !TICKER_PATTERN.matcher(ticker).matches()) {
            throw new IllegalArgumentException("Invalid ticker");
        }

        try {
            Map<?, ?> response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/price")
                            .queryParam("symbol", ticker)
                            .build())
                    .header("Authorization", "apikey " + apiKey)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("price")) {
                throw new IllegalStateException(
                        "Market data provider returned no price"
                );
            }

            double price = Double.parseDouble(
                    response.get("price").toString()
            );

            if (!Double.isFinite(price) || price <= 0) {
                throw new IllegalStateException(
                        "Market data provider returned an invalid price"
                );
            }

            return price;

        } catch (IllegalArgumentException |
                 org.springframework.web.client.RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve market price for " + ticker,
                    ex
            );
        }
    }
}
