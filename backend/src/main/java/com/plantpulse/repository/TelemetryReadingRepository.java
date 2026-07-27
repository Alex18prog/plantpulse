package com.plantpulse.repository;

import com.plantpulse.domain.TelemetryReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {

    List<TelemetryReading> findByMachineIdAndRecordedAtAfterOrderByRecordedAtAsc(Long machineId, Instant since);

    @Modifying
    @Query("delete from TelemetryReading t where t.recordedAt < :cutoff")
    int deleteByRecordedAtBefore(Instant cutoff);
}
