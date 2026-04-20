package com.ticketbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    private String name;
    private String description;
    private String venue;
    private String city;
    private String imageUrl;
}
