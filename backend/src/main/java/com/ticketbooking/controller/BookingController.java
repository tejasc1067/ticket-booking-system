package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.dto.DtoMapper;
import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestParam Long userId,
            @RequestBody BookingRequest request) {
        // TODO: Replace with authenticated user from JWT in Step 7
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingService.createBooking(user, request.getShowId(), request.getSeatIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toBookingResponse(booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId).stream()
                .map(DtoMapper::toBookingResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/reference/{ref}")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String ref) {
        Booking booking = bookingService.getBookingByReference(ref);
        return ResponseEntity.ok(DtoMapper.toBookingResponse(booking));
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId) {
        // TODO: Replace with authenticated user from JWT in Step 7
        Booking cancelled = bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok(DtoMapper.toBookingResponse(cancelled));
    }
}
