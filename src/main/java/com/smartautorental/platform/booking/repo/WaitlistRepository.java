package com.smartautorental.platform.booking.repo;

import com.smartautorental.platform.booking.model.WaitlistEntry;
import com.smartautorental.platform.booking.model.WaitlistStatus;
import com.smartautorental.platform.fleet.model.CarCategory;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    List<WaitlistEntry> findByBranchIdAndCategoryAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByCreatedAtAsc(
            Long branchId,
            CarCategory category,
            WaitlistStatus status,
            Instant endTime,
            Instant startTime
    );
}
