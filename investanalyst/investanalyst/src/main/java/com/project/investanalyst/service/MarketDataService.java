
package com.project.investanalyst.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    public List<HistoricalPrice> getHistoricalPrices(String ticker, int outputSize) {
        if (ticker == null || !TICKER_PATTERN.matcher(ticker).matches()) {
            throw new IllegalArgumentException("Invalid ticker");
        }
        if (outputSize < 2 || outputSize > 5000) {
            throw new IllegalArgumentException("Output size must be between 2 and 5000");
        }
        Map<?, ?> response;
        try {
            response = client.get()
                    .uri(uri -> uri.path("/time_series")
                            .queryParam("symbol", ticker)
                            .queryParam("interval", "1day")
                            .queryParam("outputsize", outputSize)
                            .build())
                    .header("Authorization", "apikey " + apiKey)
                    .retrieve().body(Map.class);
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new IllegalStateException("Unable to retrieve historical prices for " + ticker, ex);
        }
        if (response == null || !(response.get("values") instanceof List<?> values)
                || values.isEmpty()) {
            throw new IllegalStateException("Provider returned no historical prices for " + ticker);
        }
        List<HistoricalPrice> result = new ArrayList<>();
        Set<LocalDate> seen = new HashSet<>();
        for (Object item : values) {
            if (!(item instanceof Map<?, ?> row)) {
                throw new IllegalStateException("Malformed historical observation");
            }
            try {
                if (!(row.get("datetime") instanceof String dateText)
                        || row.get("close") == null) {
                    throw new IllegalStateException("Missing historical date or close");
                }
                LocalDate date = LocalDate.parse(dateText);
                double close = Double.parseDouble(row.get("close").toString());
                if (!Double.isFinite(close) || close <= 0 || !seen.add(date)) {
                    throw new IllegalStateException("Invalid or duplicate historical observation");
                }
                result.add(new HistoricalPrice(date, close));
            } catch (DateTimeParseException | NumberFormatException ex) {
                throw new IllegalStateException("Malformed historical date or close", ex);
            }
        }
        result.sort(Comparator.comparing(HistoricalPrice::date));
        return List.copyOf(result);
    }
}
