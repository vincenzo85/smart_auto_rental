package com.smartautorental.platform.payment.repo;

import com.smartautorental.platform.payment.model.PaymentTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
