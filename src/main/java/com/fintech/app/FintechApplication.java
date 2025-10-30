package com.fintech.app;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FintechApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintechApplication.class, args);
        System.out.println("🚀 FinTech Expense Tracker started successfully.");
    }

    @PostConstruct
    public void init() {
        // ✅ Application initialization hook
        // You can trigger configuration setup or log environment status here
        System.out.println("✅ Initialization complete. Ready to serve requests.");
    }
}
