package org.store.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.store.dto.CurrencyRate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final String NBU_API_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    public List<CurrencyRate> getExchangeRates() {
        Request request = new Request.Builder()
                .url(NBU_API_URL)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();

                if (!json.startsWith("[")) return new ArrayList<>();

                Type listType = new TypeToken<ArrayList<CurrencyRate>>(){}.getType();
                List<CurrencyRate> allRates = gson.fromJson(json, listType);

                return allRates.stream()
                        .filter(r -> r.getCc().equals("USD") || r.getCc().equals("EUR"))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}