package com.ticketbooking.controller;

import com.ticketbooking.dto.*;
import com.ticketbooking.entity.Show;
import com.ticketbooking.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ShowResponse>> getShowsByEvent(@PathVariable Long eventId) {
        List<ShowResponse> shows = showService.getShowsByEventId(eventId).stream()
                .map(DtoMapper::toShowResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(shows);
    }

    @GetMapping("/event/{eventId}/upcoming")
    public ResponseEntity<List<ShowResponse>> getUpcomingShows(@PathVariable Long eventId) {
        List<ShowResponse> shows = showService.getUpcomingShowsByEventId(eventId).stream()
                .map(DtoMapper::toShowResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(shows);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long id) {
        Show show = showService.getShowById(id);
        return ResponseEntity.ok(DtoMapper.toShowResponse(show));
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<ShowResponse> createShow(
            @PathVariable Long eventId,
            @RequestBody ShowRequest request) {
        Show show = DtoMapper.toShow(request);
        Show saved = showService.createShow(eventId, show, request.getRows(), request.getSeatsPerRow());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toShowResponse(saved));
    }
}
