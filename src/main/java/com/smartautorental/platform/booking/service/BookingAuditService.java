package com.smartautorental.platform.booking.service;

import com.smartautorental.platform.booking.model.BookingAudit;
import com.smartautorental.platform.booking.repo.BookingAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingAuditService {

    private final BookingAuditRepository bookingAuditRepository;

    public void log(Long bookingId, String eventType, String actor, String details) {
        BookingAudit audit = new BookingAudit();
        audit.setBookingId(bookingId);
        audit.setEventType(eventType);
        audit.setActor(actor);
        audit.setDetails(details);
        bookingAuditRepository.save(audit);
    }
}
