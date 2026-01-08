package org.store.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.store.dto.CurrencyRate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyService {

    // URL API Національного банку України
    private static final String NBU_API_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    public List<CurrencyRate> getExchangeRates() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            CurrencyRate[] rates = restTemplate.getForObject(NBU_API_URL, CurrencyRate[].class);

            if (rates != null) {
                // USD та EUR
                return Arrays.stream(rates)
                        .filter(rate -> "USD".equals(rate.getCc()) || "EUR".equals(rate.getCc()))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of();
    }
}