package com.ticketbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private String bookingReference;
    private String status;
    private Long showId;
    private String eventName;
    private LocalDateTime showTime;
    private Integer numberOfSeats;
    private List<SeatResponse> seats;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
