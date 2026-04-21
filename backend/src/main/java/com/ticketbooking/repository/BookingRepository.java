package com.ticketbooking.repository;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.show s JOIN FETCH s.event JOIN FETCH b.seats WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    @Query("SELECT b FROM Booking b JOIN FETCH b.show s JOIN FETCH s.event JOIN FETCH b.seats WHERE b.bookingReference = :ref")
    Optional<Booking> findByBookingReference(@Param("ref") String bookingReference);

    @Query("SELECT b FROM Booking b JOIN FETCH b.show s JOIN FETCH s.event JOIN FETCH b.seats WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);
}
