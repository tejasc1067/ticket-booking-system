package com.ticketbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {

    private Long id;
    private String seatNumber;
    private String rowName;
    private String seatType;
    private String status;
    private BigDecimal price;
}
