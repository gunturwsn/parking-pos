package com.parkee_test.parking_pos.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.parkee_test.parking_pos.entity.Ticket;
import com.parkee_test.parking_pos.entity.TicketStatus;
import com.parkee_test.parking_pos.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ParkingFeeCalculator parkingFeeCalculator;
    private final Clock clock;

    public CheckInResult checkIn(String plateNumber) {
        String error = validationPlateNumber(plateNumber);
        if (error != null) {
            return CheckInResult.error(error);
        }

        String normalizedPlate = plateNumber.trim().toUpperCase();

        Optional<Ticket> existingActive
                = ticketRepository.findByPlateNumberAndStatus(normalizedPlate, TicketStatus.ACTIVE);

        if (existingActive.isPresent()) {
            return CheckInResult.error("Vehicle already checked in");
        }

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        Ticket ticket = new Ticket();
        ticket.setPlateNumber(normalizedPlate);
        ticket.setCheckInTime(now);
        ticket.setStatus(TicketStatus.ACTIVE);

        Ticket saved = ticketRepository.save(ticket);

        return CheckInResult.ok(saved);
    }

    public CheckOutPreviewResult checkOutPreview(String plateNumber) {
        String error = validationPlateNumber(plateNumber);
        if (error != null) {
            return CheckOutPreviewResult.error(error);
        }

        String normalizedPlate = plateNumber.trim().toUpperCase();

        Optional<Ticket> existingActive
                = ticketRepository.findByPlateNumberAndStatus(normalizedPlate, TicketStatus.ACTIVE);

        if (existingActive.isPresent()) {
            Ticket ticket = existingActive.get();
            LocalDateTime checkOutTime = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
            FeeResult feeResult = parkingFeeCalculator.calculateFee(ticket.getCheckInTime(), checkOutTime);
            if (feeResult.isSuccess()) {
                return CheckOutPreviewResult.ok(ticket.getId(),
                        ticket.getPlateNumber(),
                        ticket.getCheckInTime(),
                        checkOutTime,
                        feeResult.getFee());
            } else {
                return CheckOutPreviewResult.error(feeResult.getError());
            }
        }

        return CheckOutPreviewResult.error("Active ticket not found");
    }

    public ConfirmCheckOutResult confirmCheckOut(Long ticketId) {
        String error = validationTicket(ticketId);
        if (error != null) {
            return ConfirmCheckOutResult.error(error);
        }

        Optional<Ticket> ticket = ticketRepository.findById(ticketId);

        if (ticket.isPresent()) {
            Ticket ticketExisting = ticket.get();
            if (ticketExisting.getStatus() == TicketStatus.ACTIVE) {
                LocalDateTime checkOutTime = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
                FeeResult feeResult = parkingFeeCalculator.calculateFee(ticketExisting.getCheckInTime(), checkOutTime);
                if (feeResult.isSuccess()) {
                    ticketExisting.setStatus(TicketStatus.COMPLETED);
                    ticketExisting.setCheckOutTime(checkOutTime);
                    ticketExisting.setTotalPrice(feeResult.getFee());
                    Ticket saved = ticketRepository.save(ticketExisting);
                    return ConfirmCheckOutResult.ok(saved);
                } else {
                    return ConfirmCheckOutResult.error(feeResult.getError());
                }
            } else {
                return ConfirmCheckOutResult.error("Ticket is not active");
            }
        }

        return ConfirmCheckOutResult.error("Ticket not found");
    }

    private String validationPlateNumber(String plateNumber) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            return "Plate number is required";
        }
        return null;
    }

    private String validationTicket(Long ticket) {
        if (ticket == null) {
            return "Ticket id is required";
        }
        return null;
    }
}
