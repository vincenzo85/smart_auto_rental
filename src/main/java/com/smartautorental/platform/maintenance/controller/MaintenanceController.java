package com.smartautorental.platform.maintenance.controller;

import com.smartautorental.platform.maintenance.dto.MaintenanceCreateRequest;
import com.smartautorental.platform.maintenance.dto.MaintenanceResponse;
import com.smartautorental.platform.maintenance.service.MaintenanceService;
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
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public MaintenanceResponse schedule(@Valid @RequestBody MaintenanceCreateRequest request) {
        return maintenanceService.schedule(request);
    }

    @PostMapping("/{maintenanceId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public MaintenanceResponse complete(@PathVariable Long maintenanceId) {
        return maintenanceService.complete(maintenanceId);
    }

    @GetMapping("/car/{carId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public List<MaintenanceResponse> history(@PathVariable Long carId) {
        return maintenanceService.history(carId);
    }
}
