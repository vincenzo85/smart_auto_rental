package com.smartautorental.platform.maintenance.service;

import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.repo.BookingRepository;
import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.fleet.model.Car;
import com.smartautorental.platform.fleet.model.CarStatus;
import com.smartautorental.platform.fleet.repo.CarRepository;
import com.smartautorental.platform.maintenance.dto.MaintenanceCreateRequest;
import com.smartautorental.platform.maintenance.dto.MaintenanceResponse;
import com.smartautorental.platform.maintenance.model.MaintenanceRecord;
import com.smartautorental.platform.maintenance.model.MaintenanceStatus;
import com.smartautorental.platform.maintenance.repo.MaintenanceRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public MaintenanceResponse schedule(MaintenanceCreateRequest request) {
        if (request.endTime().isBefore(request.startTime()) || request.endTime().equals(request.startTime())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Maintenance end time must be after start time");
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Car not found"));

        boolean hasConfirmedBooking = bookingRepository.existsConflictingBooking(
                car.getId(),
                request.startTime(),
                request.endTime(),
                List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT)
        );
        if (hasConfirmedBooking) {
            throw new BusinessException(ErrorCode.CONFLICT, "Cannot schedule maintenance due to active bookings");
        }

        MaintenanceRecord record = new MaintenanceRecord();
        record.setCar(car);
        record.setStartTime(request.startTime());
        record.setEndTime(request.endTime());
        record.setDescription(request.description());
        record.setStatus(MaintenanceStatus.SCHEDULED);

        if (!Instant.now().isBefore(request.startTime())) {
            record.setStatus(MaintenanceStatus.IN_PROGRESS);
            car.setStatus(CarStatus.IN_MAINTENANCE);
        }

        MaintenanceRecord saved = maintenanceRepository.save(record);
        return map(saved);
    }

    @Transactional
    public MaintenanceResponse complete(Long maintenanceId) {
        MaintenanceRecord record = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance record not found"));

        record.setStatus(MaintenanceStatus.COMPLETED);
        Car car = record.getCar();
        if (car.getStatus() == CarStatus.IN_MAINTENANCE) {
            car.setStatus(CarStatus.AVAILABLE);
        }

        return map(record);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> history(Long carId) {
        return maintenanceRepository.findByCarIdOrderByStartTimeDesc(carId).stream().map(this::map).toList();
    }

    public boolean isCarUnderMaintenance(Long carId, Instant start, Instant end) {
        return maintenanceRepository.existsOverlapping(
                carId,
                start,
                end,
                List.of(MaintenanceStatus.SCHEDULED, MaintenanceStatus.IN_PROGRESS)
        );
    }

    private MaintenanceResponse map(MaintenanceRecord record) {
        return new MaintenanceResponse(
                record.getId(),
                record.getCar().getId(),
                record.getCar().getLicensePlate(),
                record.getStartTime(),
                record.getEndTime(),
                record.getDescription(),
                record.getStatus());
    }
}
