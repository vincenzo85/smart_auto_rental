package com.smartautorental.platform.admin.controller;

import com.smartautorental.platform.admin.dto.TopRentedCarResponse;
import com.smartautorental.platform.admin.dto.UtilizationResponse;
import com.smartautorental.platform.admin.service.AdminReportService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/top-rented")
    public List<TopRentedCarResponse> topRentedCars(@RequestParam(defaultValue = "5") int limit) {
        return adminReportService.topRentedCars(limit);
    }

    @GetMapping("/utilization")
    public UtilizationResponse utilization(
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return adminReportService.utilization(branchId, from, to);
    }
}
