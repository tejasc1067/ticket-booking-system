package com.ticketbooking.repository;

import com.ticketbooking.entity.Seat;
import com.ticketbooking.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowId(Long showId);

    List<Seat> findByShowIdAndStatus(Long showId, SeatStatus status);

    List<Seat> findByIdInAndShowId(List<Long> seatIds, Long showId);

    /**
     * Pessimistic write lock — prevents concurrent reads/writes on the same seats
     * during the booking transaction. Ensures no two transactions can book the same seat.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.show.id = :showId")
    List<Seat> findByIdInAndShowIdWithLock(@Param("seatIds") List<Long> seatIds, @Param("showId") Long showId);
}
