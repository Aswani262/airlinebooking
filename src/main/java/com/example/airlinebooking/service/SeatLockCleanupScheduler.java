package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatLockJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatLockSeatJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatLockEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatLockCleanupScheduler {

    private final SeatLockJdbcRepository seatLockJdbcRepository;
    private final SeatLockSeatJdbcRepository seatLockSeatJdbcRepository;
    private final SeatJdbcRepository seatJdbcRepository;

    // every 5 minutes
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void cleanupExpiredLocks() {

        Instant now = Instant.now();

        List<SeatLockEntity> expiredLocks = seatLockJdbcRepository.findExpiredLocks(now);
        if (expiredLocks.isEmpty()) {
            return;
        }

        log.info("Found {} expired seat locks to cleanup at {}", expiredLocks.size(), now);

        for (SeatLockEntity lock : expiredLocks) {
            String lockId = lock.getId();

            List<String> seatIds = seatLockSeatJdbcRepository.findSeatIdsByLockId(lockId);

            if (!seatIds.isEmpty()) {
                int updated = seatJdbcRepository.updateStatusByIds(seatIds, SeatStatus.AVAILABLE);
                log.info("Released {} seats for lockId={}", updated, lockId);
            }

            seatLockSeatJdbcRepository.deleteByLockId(lockId);

            seatLockJdbcRepository.deleteById(lockId);

            log.info("Deleted expired lock lockId={}", lockId);
        }
    }
}
