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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody BookingRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingService.createBooking(user, request.getShowId(), request.getSeatIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toBookingResponse(booking));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BookingResponse> bookings = bookingService.getBookingsByUserId(user.getId()).stream()
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
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking cancelled = bookingService.cancelBooking(bookingId, user.getId());
        return ResponseEntity.ok(DtoMapper.toBookingResponse(cancelled));
    }
}
