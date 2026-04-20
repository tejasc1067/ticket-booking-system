package com.ticketbooking.service;

import com.ticketbooking.entity.Seat;
import com.ticketbooking.enums.SeatStatus;
import com.ticketbooking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;

    public List<Seat> getSeatsByShowId(Long showId) {
        return seatRepository.findByShowId(showId);
    }

    public List<Seat> getAvailableSeatsByShowId(Long showId) {
        return seatRepository.findByShowIdAndStatus(showId, SeatStatus.AVAILABLE);
    }

    public Seat getSeatById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seat not found with id: " + id));
    }

    public List<Seat> getSeatsByIdsAndShowId(List<Long> seatIds, Long showId) {
        List<Seat> seats = seatRepository.findByIdInAndShowId(seatIds, showId);
        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("One or more seats not found for the given show");
        }
        return seats;
    }

    @Transactional
    public void updateSeatStatus(List<Seat> seats, SeatStatus status) {
        seats.forEach(seat -> seat.setStatus(status));
        seatRepository.saveAll(seats);
    }
}
