package com.ticketbooking.service;

import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.Show;
import com.ticketbooking.enums.SeatStatus;
import com.ticketbooking.enums.SeatType;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowService {

    private final ShowRepository showRepository;
    private final EventService eventService;

    public List<Show> getShowsByEventId(Long eventId) {
        return showRepository.findByEventId(eventId);
    }

    public List<Show> getUpcomingShowsByEventId(Long eventId) {
        return showRepository.findByEventIdAndShowTimeAfter(eventId, LocalDateTime.now());
    }

    public Show getShowById(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
    }

    @Transactional
    public Show createShow(Long eventId, Show showDetails, int rows, int seatsPerRow) {
        Event event = eventService.getEventById(eventId);

        showDetails.setEvent(event);
        showDetails.setTotalSeats(rows * seatsPerRow);
        showDetails.setAvailableSeats(rows * seatsPerRow);

        Show savedShow = showRepository.save(showDetails);

        // Auto-generate seats
        List<Seat> seats = generateSeats(savedShow, rows, seatsPerRow, showDetails.getBasePrice());
        savedShow.setSeats(seats);

        return showRepository.save(savedShow);
    }

    private List<Seat> generateSeats(Show show, int rows, int seatsPerRow, BigDecimal basePrice) {
        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            String rowName = String.valueOf((char) ('A' + row));
            SeatType seatType = determineSeatType(row, rows);
            BigDecimal price = calculatePrice(basePrice, seatType);

            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = Seat.builder()
                        .show(show)
                        .seatNumber(rowName + seatNum)
                        .rowName(rowName)
                        .seatType(seatType)
                        .status(SeatStatus.AVAILABLE)
                        .price(price)
                        .build();
                seats.add(seat);
            }
        }
        return seats;
    }

    private SeatType determineSeatType(int rowIndex, int totalRows) {
        double position = (double) rowIndex / totalRows;
        if (position < 0.2) return SeatType.VIP;
        if (position < 0.5) return SeatType.PREMIUM;
        return SeatType.REGULAR;
    }

    private BigDecimal calculatePrice(BigDecimal basePrice, SeatType seatType) {
        return switch (seatType) {
            case VIP -> basePrice.multiply(BigDecimal.valueOf(2.0));
            case PREMIUM -> basePrice.multiply(BigDecimal.valueOf(1.5));
            case REGULAR -> basePrice;
        };
    }
}
