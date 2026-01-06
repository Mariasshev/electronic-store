package org.store.dto;

public class CurrencyRate {
    private String cc;   // Код валюти (USD, EUR)
    private double rate; // Курс

    // Геттери
    public String getCc() { return cc; }
    public double getRate() { return rate; }
}