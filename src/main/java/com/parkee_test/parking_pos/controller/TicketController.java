package com.parkee_test.parking_pos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkee_test.parking_pos.dto.CheckInRequest;
import com.parkee_test.parking_pos.dto.CheckInResponse;
import com.parkee_test.parking_pos.dto.CheckOutPreviewRequest;
import com.parkee_test.parking_pos.dto.CheckOutPreviewResponse;
import com.parkee_test.parking_pos.dto.ConfirmCheckOutRequest;
import com.parkee_test.parking_pos.dto.ConfirmCheckOutResponse;
import com.parkee_test.parking_pos.entity.Ticket;
import com.parkee_test.parking_pos.service.CheckInResult;
import com.parkee_test.parking_pos.service.CheckOutPreviewResult;
import com.parkee_test.parking_pos.service.ConfirmCheckOutResult;
import com.parkee_test.parking_pos.service.TicketService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest checkInRequest) {
        CheckInResult result = ticketService.checkIn(checkInRequest.getPlateNumber());

        if (!result.isSuccess()) {
            String message = result.getError();
            if ("Plate number is required".equals(message)) {
                return ResponseEntity.badRequest().body(message);
            } else if ("Vehicle is already checked in".equals(message)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
            }
            return ResponseEntity.badRequest().body(message);
        }

        Ticket ticketResult = result.getTicket();
        CheckInResponse response = new CheckInResponse(
                ticketResult.getId(),
                ticketResult.getPlateNumber(),
                ticketResult.getCheckInTime(),
                ticketResult.getStatus()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/checkout/preview")
    public ResponseEntity<?> checkOutPreview(@RequestBody CheckOutPreviewRequest checkOutPreviewRequest) {
        CheckOutPreviewResult result = ticketService.checkOutPreview(checkOutPreviewRequest.getPlateNumber());

        if (!result.isSuccess()) {
            String message = result.getError();
            if ("Plate number is required".equals(message)) {
                return ResponseEntity.badRequest().body(message);
            } else if ("Active ticket not found".equals(message)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            }
            return ResponseEntity.badRequest().body(message);
        }

        CheckOutPreviewResponse response = new CheckOutPreviewResponse(
                result.getTicketId(),
                result.getPlateNumber(),
                result.getCheckInTime(),
                result.getCheckOutTime(),
                result.getTotalPrice()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkout/confirm")
    public ResponseEntity<?> confirmCheckOut(@RequestBody ConfirmCheckOutRequest confirmCheckOutRequest) {
        ConfirmCheckOutResult result = ticketService.confirmCheckOut(confirmCheckOutRequest.getTicketId());

        if (!result.isSuccess()) {
            String message = result.getError();
            if ("Ticket id is required".equals(message)) {
                return ResponseEntity.badRequest().body(message);
            } else if ("Ticket not found".equals(message)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            } else if ("Ticket is not active".equals(message)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
            }
            return ResponseEntity.badRequest().body(message);
        }

        Ticket ticket = result.getTicket();

        ConfirmCheckOutResponse response = new ConfirmCheckOutResponse(
                ticket.getId(),
                ticket.getPlateNumber(),
                ticket.getCheckInTime(),
                ticket.getCheckOutTime(),
                ticket.getTotalPrice(),
                ticket.getStatus()
        );
        return ResponseEntity.ok(response);
    }

}
