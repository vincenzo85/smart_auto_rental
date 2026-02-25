package com.smartautorental.platform.integration.controller;

import com.smartautorental.platform.booking.dto.BookingResponse;
import com.smartautorental.platform.booking.service.BookingService;
import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.fleet.dto.AvailabilityCarResponse;
import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.service.AvailabilityService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class PartnerIntegrationController {

    private final AvailabilityService availabilityService;
    private final BookingService bookingService;

    @Value("${app.integrations.api-key}")
    private String integrationApiKey;

    @GetMapping("/availability")
    public List<AvailabilityCarResponse> availability(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @RequestParam(required = false) CarCategory category) {
        requireApiKey(apiKey);
        return availabilityService.search(branchId, startTime, endTime, category);
    }

    @GetMapping("/bookings/status")
    public BookingResponse bookingStatus(@RequestHeader("X-API-KEY") String apiKey,
                                         @RequestParam Long bookingId) {
        requireApiKey(apiKey);
        return bookingService.getByIdForIntegration(bookingId);
    }

    private void requireApiKey(String providedApiKey) {
        if (!integrationApiKey.equals(providedApiKey)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid integration API key");
        }
    }
}
