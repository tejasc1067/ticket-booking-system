package com.ticketbooking.controller;

import com.ticketbooking.dto.DtoMapper;
import com.ticketbooking.dto.SeatLockRequest;
import com.ticketbooking.dto.SeatResponse;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.service.SeatLockService;
import com.ticketbooking.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final SeatLockService seatLockService;
    private final UserRepository userRepository;

    @GetMapping("/show/{showId}")
    public ResponseEntity<List<SeatResponse>> getSeatsByShow(@PathVariable Long showId) {
        List<SeatResponse> seats = seatService.getSeatsByShowId(showId).stream()
                .map(DtoMapper::toSeatResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/show/{showId}/available")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long showId) {
        List<SeatResponse> seats = seatService.getAvailableSeatsByShowId(showId).stream()
                .map(DtoMapper::toSeatResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(seats);
    }

    @PostMapping("/lock")
    public ResponseEntity<Map<String, Object>> lockSeats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SeatLockRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean locked = seatLockService.lockSeats(request.getSeatIds(), user.getId());

        if (locked) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Seats locked successfully. You have 5 minutes to complete your booking.",
                    "seatIds", request.getSeatIds()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "One or more seats are already locked by another user. Please try different seats."
            ));
        }
    }

    @PostMapping("/unlock")
    public ResponseEntity<Map<String, Object>> unlockSeats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SeatLockRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        seatLockService.unlockSeats(request.getSeatIds(), user.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Seats unlocked successfully"
        ));
    }
}
