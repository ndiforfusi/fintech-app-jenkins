package com.fintech.app.repository;

import com.fintech.app.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 🔄 Existing: all expenses this month
    List<Expense> findByDateBetween(LocalDate start, LocalDate end);

    // 🆕 New: all expenses for a specific card this month
    List<Expense> findByCardIdAndDateBetween(Long cardId, LocalDate start, LocalDate end);
}
