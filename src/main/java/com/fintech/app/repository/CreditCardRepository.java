package com.fintech.app.repository;

import com.fintech.app.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    // 🔍 Case-insensitive search by card holder name (for filtering or
    // autocomplete)
    List<CreditCard> findByCardHolderNameContainingIgnoreCase(String name);

    // 🔍 Optional: search by type (e.g. Visa, MasterCard)
    List<CreditCard> findByCardTypeIgnoreCase(String cardType);

    // 🔍 Optional: find expired cards (could be useful for alerts)
    List<CreditCard> findByExpiryBefore(java.time.LocalDate date);
}
