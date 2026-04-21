package com.ticketbooking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for temporarily locking seats using Redis.
 * When a user selects seats, they are locked for a configurable duration
 * to prevent other users from booking the same seats concurrently.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String SEAT_LOCK_PREFIX = "seat:lock:";
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    /**
     * Attempts to lock a list of seats for a given user.
     * Uses Redis SET with NX (set if not exists) for atomic locking.
     *
     * @return true if ALL seats were locked successfully, false otherwise
     */
    public boolean lockSeats(List<Long> seatIds, Long userId) {
        List<Long> lockedSeats = new ArrayList<>();

        try {
            for (Long seatId : seatIds) {
                String key = SEAT_LOCK_PREFIX + seatId;
                Boolean success = redisTemplate.opsForValue()
                        .setIfAbsent(key, userId.toString(), LOCK_DURATION);

                if (Boolean.TRUE.equals(success)) {
                    lockedSeats.add(seatId);
                    log.info("Seat {} locked by user {}", seatId, userId);
                } else {
                    // Check if the same user already holds the lock
                    String existingUser = redisTemplate.opsForValue().get(key);
                    if (userId.toString().equals(existingUser)) {
                        lockedSeats.add(seatId);
                        log.info("Seat {} already locked by same user {}", seatId, userId);
                    } else {
                        // Another user holds the lock — rollback all locks
                        log.warn("Seat {} already locked by user {}. Rolling back.", seatId, existingUser);
                        unlockSeats(lockedSeats, userId);
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error locking seats: {}", e.getMessage());
            unlockSeats(lockedSeats, userId);
            return false;
        }
    }

    /**
     * Unlocks seats held by a specific user.
     * Only releases if the lock is still owned by the same user.
     */
    public void unlockSeats(List<Long> seatIds, Long userId) {
        for (Long seatId : seatIds) {
            String key = SEAT_LOCK_PREFIX + seatId;
            String existingUser = redisTemplate.opsForValue().get(key);

            if (userId.toString().equals(existingUser)) {
                redisTemplate.delete(key);
                log.info("Seat {} unlocked by user {}", seatId, userId);
            }
        }
    }

    /**
     * Checks if a seat is currently locked.
     */
    public boolean isSeatLocked(Long seatId) {
        String key = SEAT_LOCK_PREFIX + seatId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Gets the user ID that holds the lock on a seat.
     */
    public String getLockHolder(Long seatId) {
        String key = SEAT_LOCK_PREFIX + seatId;
        return redisTemplate.opsForValue().get(key);
    }
}
