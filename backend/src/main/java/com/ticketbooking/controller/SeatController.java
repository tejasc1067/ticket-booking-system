package com.ticketbooking.controller;

import com.ticketbooking.dto.DtoMapper;
import com.ticketbooking.dto.SeatResponse;
import com.ticketbooking.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

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
}
