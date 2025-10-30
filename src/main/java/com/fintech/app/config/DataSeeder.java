package com.fintech.app.config;

import com.fintech.app.entity.CreditCard;
import com.fintech.app.entity.Expense;
import com.fintech.app.repository.CreditCardRepository;
import com.fintech.app.service.ExpenseService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder {

    private final CreditCardRepository creditCardRepo;
    private final ExpenseService expenseService;

    public DataSeeder(CreditCardRepository creditCardRepo, ExpenseService expenseService) {
        this.creditCardRepo = creditCardRepo;
        this.expenseService = expenseService;
    }

    @PostConstruct
    public void seed() {
        if (creditCardRepo.count() == 0) {
            // 🟦 Visa Card
            CreditCard visa = new CreditCard();
            visa.setCardHolderName("Alex Johnson");
            visa.setCardType("Visa");
            visa.setCreditLimit(5000.0);
            visa.setMaskedNumber("**** **** **** 1234");
            visa.setExpiry(LocalDate.of(2026, 12, 31));
            creditCardRepo.save(visa);

            // 🟥 MasterCard
            CreditCard masterCard = new CreditCard();
            masterCard.setCardHolderName("Maria Gomez");
            masterCard.setCardType("MasterCard");
            masterCard.setCreditLimit(7500.0);
            masterCard.setMaskedNumber("**** **** **** 5678");
            masterCard.setExpiry(LocalDate.of(2025, 9, 30));
            creditCardRepo.save(masterCard);

            // 🛒 Sample Expense on Visa
            Expense expense = new Expense();
            expense.setVendor("Whole Foods");
            expense.setAmount(85.25);
            expense.setCategory("Groceries");
            expense.setDate(LocalDate.now().minusDays(2));
            expense.setCard(visa);

            expenseService.save(expense);

            System.out.println("✅ Sample data seeded: 2 credit cards and 1 expense added.");
        } else {
            System.out.println("ℹ️ Data already exists. Skipping seeding.");
        }
    }
}
