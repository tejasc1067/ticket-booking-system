package com.ticketbooking.service;

import com.ticketbooking.entity.*;
import com.ticketbooking.enums.BookingStatus;
import com.ticketbooking.enums.SeatStatus;
import com.ticketbooking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowService showService;
    private final SeatService seatService;

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found with reference: " + bookingReference));
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    @Transactional
    public Booking createBooking(User user, Long showId, List<Long> seatIds) {
        Show show = showService.getShowById(showId);
        List<Seat> seats = seatService.getSeatsByIdsAndShowId(seatIds, showId);

        // Validate all seats are available
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update seat status to BOOKED
        seatService.updateSeatStatus(seats, SeatStatus.BOOKED);

        // Update available seats count
        show.setAvailableSeats(show.getAvailableSeats() - seats.size());

        // Create booking
        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(user)
                .show(show)
                .seats(seats)
                .numberOfSeats(seats.size())
                .totalAmount(totalAmount)
                .status(BookingStatus.CONFIRMED)
                .build();

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId);

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        // Release seats
        seatService.updateSeatStatus(booking.getSeats(), SeatStatus.AVAILABLE);

        // Update available seats count
        Show show = booking.getShow();
        show.setAvailableSeats(show.getAvailableSeats() + booking.getNumberOfSeats());

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    private String generateBookingReference() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
