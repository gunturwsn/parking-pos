package com.parkee_test.parking_pos.service;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckOutPreviewResult {

    private final boolean success;
    private final Long ticketId;
    private final String plateNumber;
    private final LocalDateTime checkInTime;
    private final LocalDateTime checkOutTime;
    private final Integer totalPrice;
    private final String error;

    public static CheckOutPreviewResult ok(Long ticketId, String plateNumber, LocalDateTime checkInTime, LocalDateTime checkOutTime, Integer totalPrice) {
        return new CheckOutPreviewResult(true, ticketId, plateNumber, checkInTime, checkOutTime, totalPrice, null);
    }

    public static CheckOutPreviewResult error(String message) {
        return new CheckOutPreviewResult(false, null, null, null, null, null, message);
    }
}
