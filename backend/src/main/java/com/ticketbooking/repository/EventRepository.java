package com.ticketbooking.repository;

import com.ticketbooking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCity(String city);

    List<Event> findByNameContainingIgnoreCase(String name);
}
