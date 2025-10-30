package com.fintech.app.service;

import com.fintech.app.entity.Expense;
import com.fintech.app.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartServiceImpl implements ChartService {

    private final ExpenseRepository expenseRepo;

    public ChartServiceImpl(ExpenseRepository expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    private List<Expense> getCurrentMonthExpenses(Long cardId) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        return expenseRepo.findByCardIdAndDateBetween(cardId, start, end);
    }

    @Override
    public Map<String, Double> getCategoryTotals(Long cardId) {
        return getCurrentMonthExpenses(cardId).stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        TreeMap::new, // Sorted by category
                        Collectors.summingDouble(Expense::getAmount)));
    }

    @Override
    public Map<String, Double> getVendorTotals(Long cardId) {
        return getCurrentMonthExpenses(cardId).stream()
                .collect(Collectors.groupingBy(
                        Expense::getVendor,
                        TreeMap::new, // Sorted by vendor
                        Collectors.summingDouble(Expense::getAmount)));
    }

    @Override
    public Map<String, Double> getDailySpend(Long cardId) {
        return getCurrentMonthExpenses(cardId).stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDate().toString(), // "YYYY-MM-DD"
                        TreeMap::new, // keep dates sorted
                        Collectors.summingDouble(Expense::getAmount)));
    }
}
