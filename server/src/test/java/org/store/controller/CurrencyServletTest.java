package org.store.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.store.dto.CurrencyRate;
import org.store.service.CurrencyService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CurrencyServletTest {

    private CurrencyServlet servlet;

    @Mock private CurrencyService currencyService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new CurrencyServlet(currencyService);
    }

    @Test
    void testDoGetReturnsJson() throws Exception {
        List<CurrencyRate> mockRates = List.of(
                new CurrencyRate("USD", 38.5),
                new CurrencyRate("EUR", 42.0)
        );
        when(currencyService.getExchangeRates()).thenReturn(mockRates);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        String output = stringWriter.toString();
        assertTrue(output.contains("USD"));
        assertTrue(output.contains("38.5"));
    }
}