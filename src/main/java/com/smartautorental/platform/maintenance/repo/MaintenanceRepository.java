package com.smartautorental.platform.maintenance.repo;

import com.smartautorental.platform.maintenance.model.MaintenanceRecord;
import com.smartautorental.platform.maintenance.model.MaintenanceStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceRepository extends JpaRepository<MaintenanceRecord, Long> {

    @Query("""
            select (count(m) > 0) from MaintenanceRecord m
            where m.car.id = :carId
              and m.status in :statuses
              and m.startTime < :endTime
              and m.endTime > :startTime
            """)
    boolean existsOverlapping(@Param("carId") Long carId,
                              @Param("startTime") Instant startTime,
                              @Param("endTime") Instant endTime,
                              @Param("statuses") Collection<MaintenanceStatus> statuses);

    List<MaintenanceRecord> findByCarIdOrderByStartTimeDesc(Long carId);
}
