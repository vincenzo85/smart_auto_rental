package com.smartautorental.platform.fleet.service;

import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.repo.BookingRepository;
import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.fleet.dto.AvailabilityCarResponse;
import com.smartautorental.platform.fleet.model.Car;
import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.model.CarStatus;
import com.smartautorental.platform.fleet.repo.CarRepository;
import com.smartautorental.platform.maintenance.service.MaintenanceService;
import com.smartautorental.platform.pricing.dto.PriceQuote;
import com.smartautorental.platform.pricing.service.PricingService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final MaintenanceService maintenanceService;
    private final PricingService pricingService;

    @Transactional(readOnly = true)
    public List<AvailabilityCarResponse> search(Long branchId,
                                                Instant startTime,
                                                Instant endTime,
                                                CarCategory category) {
        if (startTime.isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Start time cannot be in the past");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "End time must be after start time");
        }

        List<Car> candidates = category == null
                ? carRepository.findByBranchIdAndStatus(branchId, CarStatus.AVAILABLE)
                : carRepository.findByBranchIdAndCategoryAndStatus(branchId, category, CarStatus.AVAILABLE);

        return candidates.stream()
                .filter(car -> !hasConflict(car, startTime, endTime))
                .map(car -> {
                    long availableCars = pricingService.estimateAvailableCarsForCategory(branchId, car.getCategory());
                    PriceQuote quote = pricingService.quote(car, startTime, endTime, false, null, availableCars);
                    return new AvailabilityCarResponse(
                            car.getId(),
                            car.getLicensePlate(),
                            car.getBrand(),
                            car.getModel(),
                            car.getCategory(),
                            quote.total(),
                            quote.dynamicFactor());
                })
                .toList();
    }

    private boolean hasConflict(Car car, Instant startTime, Instant endTime) {
        boolean bookingConflict = bookingRepository.existsConflictingBooking(
                car.getId(),
                startTime,
                endTime,
                List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT));

        boolean maintenanceConflict = maintenanceService.isCarUnderMaintenance(car.getId(), startTime, endTime);

        return bookingConflict || maintenanceConflict;
    }
}
