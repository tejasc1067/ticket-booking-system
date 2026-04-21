package com.ticketbooking.repository;

import com.ticketbooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s JOIN FETCH s.event WHERE s.event.id = :eventId")
    List<Show> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT s FROM Show s JOIN FETCH s.event WHERE s.event.id = :eventId AND s.showTime > :dateTime")
    List<Show> findByEventIdAndShowTimeAfter(@Param("eventId") Long eventId, @Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT s FROM Show s JOIN FETCH s.event WHERE s.id = :id")
    Optional<Show> findByIdWithEvent(@Param("id") Long id);
}
