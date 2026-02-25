package com.smartautorental.platform.admin.dto;

public record TopRentedCarResponse(
        Long carId,
        String licensePlate,
        String brand,
        String model,
        long rentalCount
) {
}
