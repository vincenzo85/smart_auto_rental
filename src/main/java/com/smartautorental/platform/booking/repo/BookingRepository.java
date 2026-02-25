package com.smartautorental.platform.booking.repo;

import com.smartautorental.platform.booking.model.Booking;
import com.smartautorental.platform.booking.model.BookingStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select (count(b) > 0) from Booking b
            where b.car.id = :carId
              and b.status in :statuses
              and b.startTime < :endTime
              and b.endTime > :startTime
            """)
    boolean existsConflictingBooking(@Param("carId") Long carId,
                                     @Param("startTime") Instant startTime,
                                     @Param("endTime") Instant endTime,
                                     @Param("statuses") Collection<BookingStatus> statuses);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("""
            select b from Booking b
            join fetch b.car c
            join fetch b.branch br
            where b.id = :bookingId
            """)
    Optional<Booking> findDetailedById(@Param("bookingId") Long bookingId);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, Instant threshold);

    @Query("""
            select b from Booking b
            where b.branch.id = :branchId
              and b.status = :status
              and b.startTime < :to
              and b.endTime > :from
            """)
    List<Booking> findForBranchAndRange(@Param("branchId") Long branchId,
                                        @Param("status") BookingStatus status,
                                        @Param("from") Instant from,
                                        @Param("to") Instant to);

    @Query("""
            select b.car.id as carId,
                   b.car.licensePlate as licensePlate,
                   b.car.brand as brand,
                   b.car.model as model,
                   count(b.id) as rentalCount
            from Booking b
            where b.status = 'CONFIRMED'
            group by b.car.id, b.car.licensePlate, b.car.brand, b.car.model
            order by count(b.id) desc
            """)
    List<TopCarProjection> findTopRentedCars();

    interface TopCarProjection {
        Long getCarId();
        String getLicensePlate();
        String getBrand();
        String getModel();
        long getRentalCount();
    }
}
