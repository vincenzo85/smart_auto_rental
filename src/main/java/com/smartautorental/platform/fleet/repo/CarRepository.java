package com.smartautorental.platform.fleet.repo;

import com.smartautorental.platform.fleet.model.Car;
import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.model.CarStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsByLicensePlateIgnoreCase(String licensePlate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Car c where c.id = :carId")
    Optional<Car> findByIdForUpdate(@Param("carId") Long carId);

    List<Car> findByBranchIdAndStatus(Long branchId, CarStatus status);

    List<Car> findByBranchIdAndCategoryAndStatus(Long branchId, CarCategory category, CarStatus status);

    @Query("""
            select c from Car c
            where (:branchId is null or c.branch.id = :branchId)
            and (:category is null or c.category = :category)
            and (:status is null or c.status = :status)
            order by c.id desc
            """)
    List<Car> search(@Param("branchId") Long branchId,
                     @Param("category") CarCategory category,
                     @Param("status") CarStatus status);

    long countByBranchIdAndCategoryAndStatus(Long branchId, CarCategory category, CarStatus status);

    long countByBranchId(Long branchId);
}
