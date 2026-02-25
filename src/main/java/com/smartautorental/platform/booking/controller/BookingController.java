package com.smartautorental.platform.booking.controller;

import com.smartautorental.platform.booking.dto.BookingAuditResponse;
import com.smartautorental.platform.booking.dto.BookingCreateRequest;
import com.smartautorental.platform.booking.dto.BookingResponse;
import com.smartautorental.platform.booking.dto.CancelBookingResponse;
import com.smartautorental.platform.booking.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
    public BookingResponse create(@Valid @RequestBody BookingCreateRequest request) {
        return bookingService.create(request);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
    public BookingResponse getById(@PathVariable Long bookingId) {
        return bookingService.getById(bookingId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
    public List<BookingResponse> myBookings() {
        return bookingService.myBookings();
    }

    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
    public CancelBookingResponse cancel(@PathVariable Long bookingId) {
        return bookingService.cancel(bookingId);
    }

    @GetMapping("/{bookingId}/audit")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
    public List<BookingAuditResponse> auditTrail(@PathVariable Long bookingId) {
        return bookingService.auditTrail(bookingId);
    }
}
