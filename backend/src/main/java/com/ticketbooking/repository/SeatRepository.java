package com.ticketbooking.repository;

import com.ticketbooking.entity.Seat;
import com.ticketbooking.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowId(Long showId);

    List<Seat> findByShowIdAndStatus(Long showId, SeatStatus status);

    List<Seat> findByIdInAndShowId(List<Long> seatIds, Long showId);
}
