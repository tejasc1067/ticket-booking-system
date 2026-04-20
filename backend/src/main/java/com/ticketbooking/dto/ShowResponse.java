package com.ticketbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {

    private Long id;
    private Long eventId;
    private String eventName;
    private LocalDateTime showTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal basePrice;
    private LocalDateTime createdAt;
}
