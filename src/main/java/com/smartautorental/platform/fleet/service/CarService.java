package com.smartautorental.platform.fleet.service;

import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.fleet.dto.CarResponse;
import com.smartautorental.platform.fleet.dto.CarUpsertRequest;
import com.smartautorental.platform.fleet.model.Branch;
import com.smartautorental.platform.fleet.model.Car;
import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.model.CarStatus;
import com.smartautorental.platform.fleet.repo.BranchRepository;
import com.smartautorental.platform.fleet.repo.CarRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final BranchRepository branchRepository;

    @Transactional
    public CarResponse create(CarUpsertRequest request) {
        if (carRepository.existsByLicensePlateIgnoreCase(request.licensePlate())) {
            throw new BusinessException(ErrorCode.CONFLICT, "License plate already exists");
        }

        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Branch not found"));

        Car car = new Car();
        updateEntity(car, request, branch);
        return map(carRepository.save(car));
    }

    @Transactional
    public CarResponse update(Long carId, CarUpsertRequest request) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Car not found"));

        if (!car.getLicensePlate().equalsIgnoreCase(request.licensePlate())
                && carRepository.existsByLicensePlateIgnoreCase(request.licensePlate())) {
            throw new BusinessException(ErrorCode.CONFLICT, "License plate already exists");
        }

        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Branch not found"));

        updateEntity(car, request, branch);
        return map(carRepository.save(car));
    }

    @Transactional(readOnly = true)
    public CarResponse getById(Long carId) {
        return carRepository.findById(carId)
                .map(this::map)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Car not found"));
    }

    @Transactional(readOnly = true)
    public List<CarResponse> search(Long branchId, CarCategory category, CarStatus status) {
        return carRepository.search(branchId, category, status).stream().map(this::map).toList();
    }

    @Transactional
    public void delete(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Car not found");
        }
        carRepository.deleteById(carId);
    }

    private void updateEntity(Car car, CarUpsertRequest request, Branch branch) {
        car.setLicensePlate(request.licensePlate().toUpperCase());
        car.setBrand(request.brand());
        car.setModel(request.model());
        car.setCategory(request.category());
        car.setStatus(request.status());
        car.setBaseDailyRate(request.baseDailyRate());
        car.setBranch(branch);
    }

    public CarResponse map(Car car) {
        return new CarResponse(
                car.getId(),
                car.getLicensePlate(),
                car.getBrand(),
                car.getModel(),
                car.getCategory(),
                car.getBranch().getId(),
                car.getBranch().getName(),
                car.getStatus(),
                car.getBaseDailyRate());
    }
}
