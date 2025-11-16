package com.parkee_test.parking_pos.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CheckOutPreviewResponse {

    private Long ticketId;
    private String plateNumber;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer totalPrice;
}
