package com.ticketbooking.controller;

import com.ticketbooking.dto.*;
import com.ticketbooking.entity.Event;
import com.ticketbooking.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents().stream()
                .map(DtoMapper::toEventResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(DtoMapper.toEventResponse(event));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<EventResponse>> getEventsByCity(@PathVariable String city) {
        List<EventResponse> events = eventService.getEventsByCity(city).stream()
                .map(DtoMapper::toEventResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> searchEvents(@RequestParam String keyword) {
        List<EventResponse> events = eventService.searchEvents(keyword).stream()
                .map(DtoMapper::toEventResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request) {
        Event event = DtoMapper.toEvent(request);
        Event saved = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toEventResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @RequestBody EventRequest request) {
        Event eventDetails = DtoMapper.toEvent(request);
        Event updated = eventService.updateEvent(id, eventDetails);
        return ResponseEntity.ok(DtoMapper.toEventResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
