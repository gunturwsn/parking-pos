package com.parkee_test.parking_pos.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkee_test.parking_pos.entity.Ticket;
import com.parkee_test.parking_pos.entity.TicketStatus;
import com.parkee_test.parking_pos.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ParkingFeeCalculator parkingFeeCalculator;

    private TicketService ticketService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2025-11-15T10:15:00z"), ZoneOffset.UTC);
        ticketService = new TicketService(ticketRepository, parkingFeeCalculator, fixedClock);
    }

    @Test
    void checkInShouldCreateActiveTicketWhenNonActiveTicketExist() {
        String plateNumber = "ABC123";

        when(ticketRepository.findByPlateNumberAndStatus(plateNumber, TicketStatus.ACTIVE)).thenReturn(Optional.empty());

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1L);
            return ticket;
        });

        CheckInResult checkInResult = ticketService.checkIn(plateNumber);

        assertTrue(checkInResult.isSuccess());
        assertNull(checkInResult.getError());
        assertNotNull(checkInResult.getTicket());
        assertEquals(1L, checkInResult.getTicket().getId());
        assertEquals(plateNumber, checkInResult.getTicket().getPlateNumber());
        assertEquals(TicketStatus.ACTIVE, checkInResult.getTicket().getStatus());
        assertEquals(LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone()),
                checkInResult.getTicket().getCheckInTime());

        verify(ticketRepository).findByPlateNumberAndStatus(plateNumber, TicketStatus.ACTIVE);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void checkInShouldReturnErrorWhenPlateNumberIsBlank() {
        CheckInResult checkInResult = ticketService.checkIn(" ");

        assertFalse(checkInResult.isSuccess());
        assertNull(checkInResult.getTicket());
        assertNotNull(checkInResult.getError());
        assertEquals("Plate number is required", checkInResult.getError());

        verifyNoInteractions(ticketRepository);
    }

    @Test
    void checkInShouldReturnErrorWhenActiveTicketAlreadyExist() {
        String plateNumber = "ABC123";

        Ticket ticketExisting = new Ticket();
        ticketExisting.setId(1L);
        ticketExisting.setPlateNumber(plateNumber);
        ticketExisting.setStatus(TicketStatus.ACTIVE);
        ticketExisting.setCheckInTime(LocalDateTime.now());

        when(ticketRepository.findByPlateNumberAndStatus(plateNumber, TicketStatus.ACTIVE)).thenReturn(Optional.of(ticketExisting));

        CheckInResult checkInResult = ticketService.checkIn(plateNumber);

        assertFalse(checkInResult.isSuccess());
        assertNull(checkInResult.getTicket());
        assertNotNull(checkInResult.getError());
        assertEquals("Vehicle already checked in", checkInResult.getError());

        verify(ticketRepository).findByPlateNumberAndStatus(plateNumber, TicketStatus.ACTIVE);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void previewCheckOutShouldReturnTicketAndFeeWhenActiveTicketExist() {
        String plateNumber = "ABC123";

        Ticket ticketExisting = new Ticket();
        ticketExisting.setId(1L);
        ticketExisting.setPlateNumber(plateNumber);
        ticketExisting.setStatus(TicketStatus.ACTIVE);
        ticketExisting.setCheckInTime(LocalDateTime.of(2025, 11, 15, 8, 0));

        when(ticketRepository.findByPlateNumberAndStatus(plateNumber, TicketStatus.ACTIVE)).thenReturn(Optional.of(ticketExisting));

        LocalDateTime expectedCheckOutTime = LocalDateTime.of(2025, 11, 15, 10, 15);

        FeeResult feeResult = FeeResult.ok(9000);
        when(parkingFeeCalculator.calculateFee(ticketExisting.getCheckInTime(), expectedCheckOutTime)).thenReturn(feeResult);

        CheckOutPreviewResult checkOutPreviewResult = ticketService.checkOutPreview(plateNumber);

        assertTrue(checkOutPreviewResult.isSuccess());
        assertNull(checkOutPreviewResult.getError());
        assertEquals(1L, checkOutPreviewResult.getTicketId());
        assertEquals(checkOutPreviewResult.getPlateNumber(), checkOutPreviewResult.getPlateNumber());
        assertEquals(checkOutPreviewResult.getCheckInTime(), checkOutPreviewResult.getCheckInTime());
        assertEquals(expectedCheckOutTime, checkOutPreviewResult.getCheckOutTime());
        assertEquals(9000, checkOutPreviewResult.getTotalPrice());

        verify(ticketRepository).findByPlateNumberAndStatus(plateNumber.toUpperCase(), TicketStatus.ACTIVE);
        verify(parkingFeeCalculator).calculateFee(ticketExisting.getCheckInTime(), expectedCheckOutTime);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void previewCheckOutShouldReturnErrorWhenPlateNumberIsBlank() {
        CheckOutPreviewResult checkOutPreviewResult = ticketService.checkOutPreview(" ");

        assertFalse(checkOutPreviewResult.isSuccess());
        assertNull(checkOutPreviewResult.getTicketId());
        assertNull(checkOutPreviewResult.getPlateNumber());
        assertNull(checkOutPreviewResult.getCheckInTime());
        assertNull(checkOutPreviewResult.getCheckOutTime());
        assertNull(checkOutPreviewResult.getTotalPrice());
        assertNotNull(checkOutPreviewResult.getError());
        assertEquals("Plate number is required", checkOutPreviewResult.getError());

        verifyNoInteractions(ticketRepository);
        verifyNoInteractions(parkingFeeCalculator);
    }

    @Test
    void previewCheckOutShouldReturnErrorWhenNoActiveTicketFound() {
        String plateNumber = "ABC123";

        when(ticketRepository.findByPlateNumberAndStatus(plateNumber, TicketStatus.ACTIVE)).thenReturn(Optional.empty());

        CheckOutPreviewResult checkOutPreviewResult = ticketService.checkOutPreview(plateNumber);
        assertFalse(checkOutPreviewResult.isSuccess());
        assertNull(checkOutPreviewResult.getTicketId());
        assertNull(checkOutPreviewResult.getPlateNumber());
        assertNull(checkOutPreviewResult.getCheckInTime());
        assertNull(checkOutPreviewResult.getCheckOutTime());
        assertNull(checkOutPreviewResult.getTotalPrice());
        assertNotNull(checkOutPreviewResult.getError());
        assertEquals("Active ticket not found", checkOutPreviewResult.getError());

        verify(ticketRepository).findByPlateNumberAndStatus(plateNumber.toUpperCase(), TicketStatus.ACTIVE);
        verifyNoInteractions(parkingFeeCalculator);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void confirmCheckOutShouldCompleteTicketWhenActiveTicketExist() {
        Long ticketId = 1L;

        Ticket ticketExisting = new Ticket();
        ticketExisting.setId(ticketId);
        ticketExisting.setPlateNumber("ABC123");
        ticketExisting.setStatus(TicketStatus.ACTIVE);
        ticketExisting.setCheckInTime(LocalDateTime.of(2025, 11, 15, 8, 0));

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticketExisting));

        LocalDateTime expectedCheckOutTime = LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone());

        FeeResult feeResult = FeeResult.ok(9000);
        when(parkingFeeCalculator.calculateFee(ticketExisting.getCheckInTime(), expectedCheckOutTime)).thenReturn(feeResult);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ConfirmCheckOutResult confirmCheckOutResult = ticketService.confirmCheckOut(ticketId);
        ConfirmCheckOutResult confirmCheckOutResult = ticketService.confirmCheckOut(ticketId);

        assertTrue(confirmCheckOutResult.isSuccess());
        assertNull(confirmCheckOutResult.getError());
        assertNotNull(confirmCheckOutResult.getTicket());
        assertEquals(TicketStatus.COMPLETED, confirmCheckOutResult.getTicket().getStatus());
        assertEquals(9000, confirmCheckOutResult.getTicket().getTotalPrice());
        assertEquals(expectedCheckOutTime, confirmCheckOutResult.getTicket().getCheckOutTime());

        verify(ticketRepository).findById(ticketId);
        verify(parkingFeeCalculator).calculateFee(ticketExisting.getCheckInTime(), expectedCheckOutTime);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void confirmCheckOutShouldReturnErrorWhenTicketIdIsNull() {
        ConfirmCheckOutResult result = ticketService.confirmCheckOut(null);

        assertFalse(result.isSuccess());
        assertEquals("Ticket id is required", result.getError());
        assertNull(result.getTicket());

        verifyNoInteractions(ticketRepository);
        verifyNoInteractions(parkingFeeCalculator);
    }

    @Test
    void confirmCheckOutShouldReturnErrorWhenTicketNotFound() {
        Long ticketId = 99L;

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.empty());

        ConfirmCheckOutResult result = ticketService.confirmCheckOut(ticketId);

        assertFalse(result.isSuccess());
        assertEquals("Ticket not found", result.getError());
        assertNull(result.getTicket());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(parkingFeeCalculator);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void confirmCheckOutShouldReturnErrorWhenTicketIsNotActive() {
        Long ticketId = 2L;

        Ticket ticketExisting = new Ticket();
        ticketExisting.setId(ticketId);
        ticketExisting.setPlateNumber("ABC123");
        ticketExisting.setStatus(TicketStatus.COMPLETED);
        ticketExisting.setCheckInTime(LocalDateTime.of(2025, 11, 15, 8, 0));
        ticketExisting.setCheckOutTime(LocalDateTime.of(2025, 11, 15, 9, 0));
        ticketExisting.setTotalPrice(3000);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticketExisting));

        ConfirmCheckOutResult result = ticketService.confirmCheckOut(ticketId);

        assertFalse(result.isSuccess());
        assertEquals("Ticket is not active", result.getError());
        assertNull(result.getTicket());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(parkingFeeCalculator);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void confirmCheckOutShouldReturnErrorWhenFeeCalculationFails() {
        Long ticketId = 3L;

        Ticket ticketExisting = new Ticket();
        ticketExisting.setId(ticketId);
        ticketExisting.setPlateNumber("ABC123");
        ticketExisting.setStatus(TicketStatus.ACTIVE);
        ticketExisting.setCheckInTime(LocalDateTime.of(2025, 11, 15, 8, 0));

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticketExisting));

        LocalDateTime expectedCheckOutTime
                = LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone());

        FeeResult errorResult = FeeResult.error("Invalid time range");
        when(parkingFeeCalculator.calculateFee(ticketExisting.getCheckInTime(), expectedCheckOutTime))
                .thenReturn(errorResult);

        ConfirmCheckOutResult result = ticketService.confirmCheckOut(ticketId);

        assertFalse(result.isSuccess());
        assertEquals("Invalid time range", result.getError());
        assertNull(result.getTicket());

        verify(ticketRepository).findById(ticketId);
        verify(parkingFeeCalculator).calculateFee(ticketExisting.getCheckInTime(), expectedCheckOutTime);
        verify(ticketRepository, never()).save(any());
    }

}
