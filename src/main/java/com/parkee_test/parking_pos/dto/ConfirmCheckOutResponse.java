package com.parkee_test.parking_pos.dto;

import java.time.LocalDateTime;

import com.parkee_test.parking_pos.entity.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ConfirmCheckOutResponse {

    private Long tickedId;
    private String plateNumber;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer totalPrice;
    private TicketStatus status;
}
