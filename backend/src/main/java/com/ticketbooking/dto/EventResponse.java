package com.ticketbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String name;
    private String description;
    private String venue;
    private String city;
    private String imageUrl;
    private List<ShowResponse> shows;
    private LocalDateTime createdAt;
}
