package com.parkee_test.parking_pos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parkee_test.parking_pos.entity.Ticket;
import com.parkee_test.parking_pos.entity.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByPlateNumberAndStatus(String plateNumber, TicketStatus status);
    
}
