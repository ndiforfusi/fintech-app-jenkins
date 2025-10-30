package com.fintech.app.service;

import java.util.Map;

public interface ChartService {
    Map<String, Double> getCategoryTotals(Long cardId);

    Map<String, Double> getVendorTotals(Long cardId);

    Map<String, Double> getDailySpend(Long cardId);
}
