package org.store.dto;

public class CurrencyRate {
    private String cc;   // Код валюти (USD, EUR)
    private double rate; // Курс

    public CurrencyRate(String usd, double v) {
    }

    // Геттери
    public String getCc() { return cc; }
    public double getRate() { return rate; }
}