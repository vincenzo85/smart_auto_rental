package com.smartautorental.platform.fleet.controller;

import com.smartautorental.platform.fleet.dto.CarResponse;
import com.smartautorental.platform.fleet.dto.CarUpsertRequest;
import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.model.CarStatus;
import com.smartautorental.platform.fleet.service.CarService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public CarResponse create(@Valid @RequestBody CarUpsertRequest request) {
        return carService.create(request);
    }

    @PutMapping("/{carId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public CarResponse update(@PathVariable Long carId, @Valid @RequestBody CarUpsertRequest request) {
        return carService.update(carId, request);
    }

    @GetMapping("/{carId}")
    public CarResponse getById(@PathVariable Long carId) {
        return carService.getById(carId);
    }

    @GetMapping
    public List<CarResponse> search(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) CarCategory category,
            @RequestParam(required = false) CarStatus status) {
        return carService.search(branchId, category, status);
    }

    @DeleteMapping("/{carId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long carId) {
        carService.delete(carId);
    }
}
