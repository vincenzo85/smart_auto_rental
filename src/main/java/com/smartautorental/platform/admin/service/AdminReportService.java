package com.smartautorental.platform.admin.service;

import com.smartautorental.platform.admin.dto.TopRentedCarResponse;
import com.smartautorental.platform.admin.dto.UtilizationResponse;
import com.smartautorental.platform.booking.model.Booking;
import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.repo.BookingRepository;
import com.smartautorental.platform.fleet.repo.CarRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public List<TopRentedCarResponse> topRentedCars(int limit) {
        return bookingRepository.findTopRentedCars().stream()
                .limit(limit)
                .map(p -> new TopRentedCarResponse(
                        p.getCarId(),
                        p.getLicensePlate(),
                        p.getBrand(),
                        p.getModel(),
                        p.getRentalCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public UtilizationResponse utilization(Long branchId, Instant from, Instant to) {
        long carsInBranch = carRepository.countByBranchId(branchId);
        if (carsInBranch == 0) {
            return new UtilizationResponse(branchId, from, to, 0, BigDecimal.ZERO);
        }

        List<Booking> bookings = bookingRepository.findForBranchAndRange(branchId, BookingStatus.CONFIRMED, from, to);
        long rentedHours = bookings.stream()
                .mapToLong(b -> {
                    Instant effectiveStart = b.getStartTime().isBefore(from) ? from : b.getStartTime();
                    Instant effectiveEnd = b.getEndTime().isAfter(to) ? to : b.getEndTime();
                    return Math.max(0, Duration.between(effectiveStart, effectiveEnd).toHours());
                })
                .sum();

        long totalHours = Math.max(1, Duration.between(from, to).toHours()) * carsInBranch;
        BigDecimal utilization = BigDecimal.valueOf(rentedHours)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(totalHours), 2, RoundingMode.HALF_UP);

        return new UtilizationResponse(branchId, from, to, carsInBranch, utilization);
    }
}
