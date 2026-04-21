package com.ticketbooking.service;

import com.ticketbooking.entity.*;
import com.ticketbooking.enums.BookingStatus;
import com.ticketbooking.enums.SeatStatus;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowService showService;
    private final SeatService seatService;
    private final SeatLockService seatLockService;
    private final SeatRepository seatRepository;

    private static final int MAX_RETRIES = 3;

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

    /**
     * Creates a booking with concurrency protection:
     * 1. Redis lock verification (application-level)
     * 2. Pessimistic DB lock on seats (database-level)
     * 3. Optimistic locking via @Version (entity-level)
     * 4. Retry mechanism for optimistic lock failures
     */
    @Transactional
    public Booking createBooking(User user, Long showId, List<Long> seatIds) {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                log.info("Booking attempt {} for user {} on show {}", attempt, user.getEmail(), showId);
                return executeBooking(user, showId, seatIds);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Optimistic lock conflict on attempt {}. Retrying...", attempt);
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("Booking failed due to high demand. Please try again.");
                }
            }
        }

        throw new RuntimeException("Booking failed after maximum retries");
    }

    private Booking executeBooking(User user, Long showId, List<Long> seatIds) {
        // 1. Verify Redis locks belong to this user
        for (Long seatId : seatIds) {
            String lockHolder = seatLockService.getLockHolder(seatId);
            if (lockHolder == null) {
                throw new RuntimeException("Seat must be locked before booking. Please lock seats first.");
            }
            if (!lockHolder.equals(user.getId().toString())) {
                throw new RuntimeException("Seat is locked by another user");
            }
        }

        // 2. Acquire pessimistic lock on seats (database row-level lock)
        List<Seat> seats = seatRepository.findByIdInAndShowIdWithLock(seatIds, showId);

        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("One or more seats not found for the given show");
        }

        // 3. Validate seats are still AVAILABLE (double-check after acquiring lock)
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat " + seat.getSeatNumber() + " is no longer available");
            }
        }

        // 4. Get show and validate availability
        Show show = showService.getShowById(showId);
        if (show.getAvailableSeats() < seats.size()) {
            throw new RuntimeException("Not enough available seats for this show");
        }

        // 5. Calculate total amount
        BigDecimal totalAmount = seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Update seat status to BOOKED
        seatService.updateSeatStatus(seats, SeatStatus.BOOKED);

        // 7. Update available seats count
        show.setAvailableSeats(show.getAvailableSeats() - seats.size());

        // 8. Create and save booking
        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(user)
                .show(show)
                .seats(seats)
                .numberOfSeats(seats.size())
                .totalAmount(totalAmount)
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // 9. Release Redis locks after successful booking
        seatLockService.unlockSeats(seatIds, user.getId());
        log.info("Booking {} confirmed for user {}", savedBooking.getBookingReference(), user.getEmail());

        return savedBooking;
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
        log.info("Booking {} cancelled by user {}", booking.getBookingReference(), userId);
        return bookingRepository.save(booking);
    }

    private String generateBookingReference() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
