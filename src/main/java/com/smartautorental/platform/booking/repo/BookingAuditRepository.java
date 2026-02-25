package com.smartautorental.platform.booking.repo;

import com.smartautorental.platform.booking.model.BookingAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingAuditRepository extends JpaRepository<BookingAudit, Long> {
    List<BookingAudit> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
