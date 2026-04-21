package com.ticketbooking.dto;

import com.ticketbooking.entity.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to convert between entities and DTOs.
 */
public final class DtoMapper {

    private DtoMapper() {}

    // ========== EVENT ==========

    public static Event toEvent(EventRequest request) {
        return Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .venue(request.getVenue())
                .city(request.getCity())
                .imageUrl(request.getImageUrl())
                .build();
    }

    public static EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .venue(event.getVenue())
                .city(event.getCity())
                .imageUrl(event.getImageUrl())
                .createdAt(event.getCreatedAt())
                .build();
    }

    // ========== SHOW ==========

    public static Show toShow(ShowRequest request) {
        return Show.builder()
                .showTime(request.getShowTime())
                .basePrice(request.getBasePrice())
                .build();
    }

    public static ShowResponse toShowResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .eventId(show.getEvent().getId())
                .eventName(show.getEvent().getName())
                .showTime(show.getShowTime())
                .totalSeats(show.getTotalSeats())
                .availableSeats(show.getAvailableSeats())
                .basePrice(show.getBasePrice())
                .createdAt(show.getCreatedAt())
                .build();
    }

    // ========== SEAT ==========

    public static SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .rowName(seat.getRowName())
                .seatType(seat.getSeatType().name())
                .status(seat.getStatus().name())
                .price(seat.getPrice())
                .build();
    }

    // ========== BOOKING ==========

    public static BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .status(booking.getStatus().name())
                .showId(booking.getShow().getId())
                .eventName(booking.getShow().getEvent().getName())
                .showTime(booking.getShow().getShowTime())
                .numberOfSeats(booking.getNumberOfSeats())
                .seats(booking.getSeats().stream()
                        .map(DtoMapper::toSeatResponse)
                        .collect(Collectors.toList()))
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
