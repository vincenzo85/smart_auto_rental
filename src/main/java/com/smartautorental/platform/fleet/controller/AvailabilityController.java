package com.smartautorental.platform.fleet.controller;

import com.smartautorental.platform.fleet.dto.AvailabilityCarResponse;
import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.service.AvailabilityService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public List<AvailabilityCarResponse> search(
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @RequestParam(required = false) CarCategory category) {
        return availabilityService.search(branchId, startTime, endTime, category);
    }
}
